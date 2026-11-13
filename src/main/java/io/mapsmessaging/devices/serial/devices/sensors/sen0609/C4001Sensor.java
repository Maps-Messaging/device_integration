/*
 * Copyright [ 2024 - 2026 ] MapsMessaging B.V.
 * Licensed under the Apache License, Version 2.0 with the Commons Clause.
 * Protocol details adapted from DFRobot_C4001 (MIT), Copyright (c) 2020 DFRobot.
 */
package io.mapsmessaging.devices.serial.devices.sensors.sen0609;

import io.mapsmessaging.devices.Device;
import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.queue.HardwareQueue;
import io.mapsmessaging.devices.queue.HardwareRingBuffer;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import io.mapsmessaging.devices.serial.devices.sensors.SerialDevice;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

/** UART C4001 acquisition and hardware-style snapshot/difference FIFOs. */
public final class C4001Sensor implements Device, Sensor, HardwareQueue<C4001Observation> {
  private static final int MAX_REPORT_LENGTH = 256;
  private final SerialDevice serial;
  private final HardwareRingBuffer<C4001Observation> snapshots;
  private final HardwareRingBuffer<C4001Change> changes;
  private final C4001Differ differ = new C4001Differ();
  private final ConcurrentLinkedQueue<Setting> settings = new ConcurrentLinkedQueue<>();
  private final AtomicReference<IOException> failure = new AtomicReference<>();
  private final StringBuilder report = new StringBuilder();
  private final Thread worker;
  private volatile boolean running;
  private volatile int triggerSensitivity = -1;
  private volatile int keepSensitivity = -1;
  private volatile int requestedMode = -1;
  private C4001Observation reference;

  public C4001Sensor(SerialDevice serial) throws IOException { this(serial, 16, 64); }

  public C4001Sensor(SerialDevice serial, int snapshotCapacity, int changeCapacity) throws IOException {
    this.serial = Objects.requireNonNull(serial, "serial");
    snapshots = new HardwareRingBuffer<>(snapshotCapacity);
    changes = new HardwareRingBuffer<>(changeCapacity);
    if (!serial.isOpen() && !serial.openPort()) throw new IOException("Cannot open C4001 port " + serial.getSystemPortName());
    running = true;
    worker = Thread.ofVirtual().name("c4001-" + serial.getSystemPortName()).start(this::readLoop);
  }

  @Override public String getName() { return "SEN0609"; }
  @Override public String getDescription() { return "DFRobot C4001 24 GHz radar (UART 9600 8N1)"; }
  @Override public DeviceType getType() { return DeviceType.SENSOR; }
  @Override public List<SensorReading<?>> getReadings() { return List.of(); }
  @Override public C4001Observation getData() { return snapshots.getData(); }
  @Override public int queueDepth() { return snapshots.queueDepth(); }
  @Override public boolean hasData() { return snapshots.hasData(); }
  @Override public boolean hasOverrun() { return snapshots.hasOverrun(); }
  public HardwareQueue<C4001Change> changes() { return changes; }
  public C4001Differ differ() { return differ; }
  public IOException getLastError() { return failure.get(); }
  public int getTriggerSensitivity() { return triggerSensitivity; }
  public int getKeepSensitivity() { return keepSensitivity; }
  public int getRequestedMode() { return requestedMode; }

  /** Queued, not applied: the serial worker owns command writes and the input stream. */
  public void setHardwareSensitivity(boolean trigger, int level) throws IOException {
    if (!running) throw new IOException("Radar sensor is closed");
    if (level < 0 || level > 9) throw new IOException("Radar sensitivity must be 0..9");
    settings.offer(new Setting(trigger ? "TRIGGER" : "KEEP", level));
  }

  /** Firmware mode: 0=presence, 1=speed/distance. Applied by the serial worker. */
  public void setHardwareMode(int mode) throws IOException {
    if (!running) throw new IOException("Radar sensor is closed");
    if (mode != 0 && mode != 1) throw new IOException("C4001 mode must be 0 or 1");
    settings.offer(new Setting("MODE", mode));
  }

  public void close() {
    running = false;
    worker.interrupt();
    serial.closePort();
  }

  private void readLoop() {
    byte[] buffer = new byte[256];
    while (running && !Thread.currentThread().isInterrupted()) {
      try {
        Setting setting;
        while ((setting = settings.poll()) != null) applySetting(setting);
        int read = serial.readBytes(buffer, buffer.length);
        if (read < 0) throw new IOException("C4001 serial read failed: " + read);
        if (read == 0) {
          LockSupport.parkNanos(1_000_000L);
          continue;
        }
        accept(buffer, read);
        failure.set(null);
      } catch (IOException exception) {
        if (!running) break;
        failure.set(exception);
        LockSupport.parkNanos(10_000_000L);
      } catch (RuntimeException exception) {
        if (!running) break;
        failure.set(new IOException("C4001 stream failed", exception));
        break;
      }
    }
  }

  private void applySetting(Setting setting) throws IOException {
    // DFRobot UART configuration protocol: stop, modify, persist, restart.
    writeCommand("sensorStop");
    pause(100);
    switch (setting.kind()) {
      case "TRIGGER" -> writeCommand("setSensitivity 255 " + setting.value());
      case "KEEP" -> writeCommand("setSensitivity " + setting.value() + " 255");
      case "MODE" -> writeCommand("setRunApp " + setting.value());
      default -> throw new IOException("Unknown C4001 setting: " + setting.kind());
    }
    pause(100);
    writeCommand("saveConfig");
    pause(100);
    writeCommand("sensorStart");
    switch (setting.kind()) {
      case "TRIGGER" -> triggerSensitivity = setting.value();
      case "KEEP" -> keepSensitivity = setting.value();
      case "MODE" -> requestedMode = setting.value();
      default -> { /* Already rejected above. */ }
    }
  }

  private void writeCommand(String command) throws IOException {
    byte[] bytes = command.getBytes(StandardCharsets.US_ASCII);
    if (serial.writeBytes(bytes, bytes.length) != bytes.length) {
      throw new IOException("C4001 command write failed: " + command);
    }
  }

  private static void pause(long millis) throws IOException {
    try { Thread.sleep(millis); }
    catch (InterruptedException interrupted) {
      Thread.currentThread().interrupt();
      throw new IOException("Interrupted configuring C4001", interrupted);
    }
  }

  /** Package-private for deterministic serial fixture tests. */
  void accept(byte[] bytes, int length) {
    for (int i = 0; i < length; i++) {
      char value = (char) (bytes[i] & 255);
      if (value == '\n' || value == '\r' || value == '*') {
        completeReport();
      } else if (value >= 32 && value <= 126) {
        if (value == '$' && report.length() != 0) completeReport();
        if (report.length() < MAX_REPORT_LENGTH) report.append(value);
        else report.setLength(0);
      }
    }
  }

  private void completeReport() {
    if (report.length() == 0) return;
    String line = report.toString().trim();
    report.setLength(0);
    C4001Observation observation = parse(line, Instant.now());
    if (observation == null) return;
    snapshots.offer(observation);
    if (reference == null) {
      reference = observation;
      return;
    }
    C4001Change change = differ.diff(reference, observation);
    if (!change.changedFields().isEmpty()) {
      changes.offer(change);
      reference = observation;
    }
  }

  static C4001Observation parse(String raw, Instant timestamp) {
    int marker = raw.indexOf('$');
    if (marker < 0) return null;
    String message = raw.substring(marker);
    int star = message.indexOf('*');
    if (star >= 0) message = message.substring(0, star);
    String[] fields = message.split(",", -1);
    try {
      if (fields.length >= 2 && fields[0].equals("$DFHPD")) {
        int exists = Integer.parseInt(fields[1].trim());
        if (exists != 0 && exists != 1) return null;
        return new C4001Observation(timestamp, C4001Observation.Mode.PRESENCE, exists == 1,
            null, null, null, null);
      }
      // DFRobot's UART layout: $DFDMD,count,other,distance_m,speed_mps,energy,...*
      if (fields.length >= 6 && fields[0].equals("$DFDMD")) {
        int count = Integer.parseInt(fields[1].trim());
        if (count < 0) return null;
        if (count == 0) return new C4001Observation(timestamp, C4001Observation.Mode.SPEED,
            false, 0, null, null, null);
        double distance = Double.parseDouble(fields[3].trim());
        double speed = Double.parseDouble(fields[4].trim());
        int energy = Integer.parseInt(fields[5].trim());
        if (!Double.isFinite(distance) || !Double.isFinite(speed) || distance < 0 || energy < 0) return null;
        return new C4001Observation(timestamp, C4001Observation.Mode.SPEED, true,
            count, distance, speed, energy);
      }
    } catch (NumberFormatException ignored) {
      // Ignore truncated or malformed UART reports rather than publish partial data.
    }
    return null;
  }

  private record Setting(String kind, int value) {}
}
