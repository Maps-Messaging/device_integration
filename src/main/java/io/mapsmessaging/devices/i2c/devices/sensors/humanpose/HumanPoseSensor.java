/*
 * Copyright (c) 2026 MapsMessaging B.V. Licensed under the project's license.
 */
package io.mapsmessaging.devices.i2c.devices.sensors.humanpose;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.devices.queue.HardwareQueue;
import io.mapsmessaging.devices.queue.HardwareRingBuffer;
import io.mapsmessaging.devices.sensorreadings.IntegerSensorReading;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import io.mapsmessaging.logging.LoggerFactory;
import lombok.Getter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

/**
 * SEN0670 producer: only its worker touches the protocol after initialisation.
 * Applications consume independent, fully constructed batches from the hardware FIFO.
 */
public final class HumanPoseSensor extends I2CDevice implements Sensor, HardwareQueue<HumanPoseDataSet> {
  public static final int DEFAULT_CAPACITY = 16;

  private final HumanPoseProtocol protocol;
  private final HardwareRingBuffer<HumanPoseDataSet> fifo;
  private final ConcurrentLinkedQueue<Configuration> commands = new ConcurrentLinkedQueue<>();
  private final AtomicReference<IOException> lastError = new AtomicReference<>();
  private final Thread worker;
  @Getter private final List<SensorReading<?>> readings;
  private volatile boolean connected;
  private volatile int currentModel;
  private volatile List<String> learnedNames = List.of();

  public HumanPoseSensor(AddressableDevice device) throws IOException {
    this(device, DEFAULT_CAPACITY);
  }

  public HumanPoseSensor(AddressableDevice device, int capacity) throws IOException {
    super(device, LoggerFactory.getLogger(HumanPoseSensor.class));
    fifo = new HardwareRingBuffer<>(capacity);
    protocol = new HumanPoseProtocol(device);
    protocol.initialize();
    currentModel = protocol.getModel();
    readings = generateSensorReadings(List.of(
        new IntegerSensorReading("model", "", "Selected SEN0670 detection model", 3, true, 1, 4, () -> currentModel),
        new IntegerSensorReading("queueDepth", "", "Unread inference datasets", 0, true, 0, capacity, fifo::queueDepth)
    ));
    connected = true;
    worker = Thread.ofVirtual().name("sen0670-" + device.getBus() + "-" + device.getDevice()).start(this::acquire);
  }

  private void acquire() {
    try {
      while (connected && !Thread.currentThread().isInterrupted()) {
        Configuration next;
        while ((next = commands.poll()) != null) {
          try {
            configure(next);
            lastError.set(null);
          } catch (IOException exception) {
            lastError.set(exception);
          }
        }
        if (!connected) break;
        try {
          List<HumanPoseProtocol.Detection> detections = protocol.invoke();
          HumanPoseDataSet complete = new HumanPoseDataSet(Instant.now(), protocol.getModel(), detections);
          currentModel = complete.model();
          fifo.offer(complete);
          lastError.set(null);
        } catch (IOException exception) {
          if (!connected || Thread.currentThread().isInterrupted()) break;
          lastError.set(exception);
          Thread.sleep(100);
        }
      }
    } catch (InterruptedException interrupted) {
      Thread.currentThread().interrupt();
    } finally {
      commands.clear();
      // close() may be called from I2CDeviceScheduler with the bus lock held. Never
      // join the worker there; it owns cleanup after releasing any outstanding I/O.
      super.close();
    }
  }

  private void configure(Configuration command) throws IOException {
    switch (command.name()) {
      case "SET_MODEL" -> { protocol.setModel(command.value()); currentModel = protocol.getModel(); }
      case "SET_CONFIDENCE" -> protocol.setThreshold("TSCORE", 0x0101, command.value());
      case "SET_IOU" -> protocol.setThreshold("TIOU", 0x0103, command.value());
      case "SET_SIMILARITY" -> protocol.setThreshold("TSIMILARITY", 0x0105, command.value());
      case "SET_KEYPOINT_OUTPUT" -> {
        if (command.value() != 0 && command.value() != 1) throw new IOException("Keypoint output must be 0 or 1");
        protocol.setKeypoints(command.value() == 1);
      }
      case "GET_LEARNED_NAMES" -> learnedNames = protocol.learnedNames(command.value());
      default -> throw new IOException("Unknown SEN0670 command: " + command.name());
    }
  }

  /** Queue an operation; never block a caller holding the I2C scheduler's shared bus lock. */
  public void request(String name, int value) throws IOException {
    if (!connected) throw new IOException("SEN0670 is closed");
    switch (name) {
      case "SET_MODEL" -> {
        if (value != 1 && value != 3 && value != 4) throw new IOException("Model must be 1, 3 or 4");
      }
      case "SET_CONFIDENCE", "SET_IOU", "SET_SIMILARITY" -> {
        if (value < 0 || value > 100) throw new IOException("Threshold must be 0..100");
      }
      case "SET_KEYPOINT_OUTPUT" -> {
        if (value != 0 && value != 1) throw new IOException("Keypoint output must be 0 or 1");
      }
      case "GET_LEARNED_NAMES" -> {
        if (value != 1 && value != 3) throw new IOException("Learned-name model must be 1 or 3");
      }
      default -> throw new IOException("Unknown SEN0670 command: " + name);
    }
    commands.offer(new Configuration(name, value));
  }

  public IOException getLastError() { return lastError.get(); }
  public List<String> getLearnedNames() { return learnedNames; }

  @Override public HumanPoseDataSet getData() { return fifo.getData(); }
  @Override public int queueDepth() { return fifo.queueDepth(); }
  @Override public boolean hasData() { return fifo.hasData(); }
  @Override public boolean hasOverrun() { return fifo.hasOverrun(); }

  @Override public boolean isConnected() { return connected; }

  @Override public void close() {
    if (!connected) return;
    connected = false;
    worker.interrupt();
    // The worker closes the physical device after its current operation exits.
  }

  @Override public String getName() { return "SEN0670"; }
  @Override public String getDescription() { return "DFRobot Human Pose, Hand and Gesture sensor"; }
  @Override public DeviceType getType() { return DeviceType.SENSOR; }

  private record Configuration(String name, int value) {}
}
