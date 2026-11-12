/*
 * Copyright (c) 2026 MapsMessaging B.V.
 * Binary SEN0670 protocol port based on DFRobot_HumanPose, Copyright (c) 2026 DFRobot.
 * DFRobot's implementation is MIT licensed; preserve its copyright and MIT notice.
 */
package io.mapsmessaging.devices.i2c.devices.sensors.humanpose;

import io.mapsmessaging.devices.i2c.I2CDeviceScheduler;
import io.mapsmessaging.devices.impl.AddressableDevice;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Binary-framed AT protocol over DFRobot's I2C transport. Not thread-safe; worker owns it. */
public final class HumanPoseProtocol {
  static final int ADDRESS = 0x3a;
  private static final int TRANSFER = 50;
  private static final int FRAME_LIMIT = 32768;
  private static final int RESULT_LIMIT = 16;
  private static final List<String> GESTURES = List.of("zero", "one", "two", "three", "four", "five",
      "dislike", "like", "ok", "stop", "rock", "three2", "two_up", "no_gesture");

  public record Point(int x, int y) {}
  public record Detection(int id, String name, int x, int y, int width, int height, int score, List<Point> keypoints) {
    public Detection {
      // Lists may contain null when a landmark was absent from the hardware response.
      keypoints = java.util.Collections.unmodifiableList(new ArrayList<>(keypoints));
    }
  }

  private final AddressableDevice bus;
  private byte[] pending = new byte[0];
  private final ByteArrayOutputStream fragments = new ByteArrayOutputStream();
  private final List<Detection> detections = new ArrayList<>();
  private List<String> handNames = List.of();
  private List<String> poseNames = List.of();
  private int model = 3;
  private int responseId = -1;
  private int responseStatus;
  private Object responseValue;
  private boolean eventComplete;
  private int eventStatus;

  public HumanPoseProtocol(AddressableDevice bus) {
    this.bus = bus;
  }

  public void initialize() throws IOException {
    command("TPROTO=1", 0x1f, 1200);
    // Packet size tuning is advisory in the vendor driver.
    try { command("TPKTSZ=192", 0x21, 1200); }
    catch (IOException ignored) { /* Older firmware may not implement packet tuning. */ }
    command("TKPTS=1", 0x37, 1200);
    Object identified = command("NAME?", 3, 3000);
    if (!(identified instanceof String name) || !name.contains("DFRobot Human Pose")) {
      throw new IOException("SEN0670 identification failed: " + identified);
    }
  }

  public int getModel() { return model; }

  public void setModel(int value) throws IOException {
    if (value != 1 && value != 3 && value != 4) throw new IOException("Invalid SEN0670 model " + value);
    command("MODEL=" + value, 0x0b, 3000);
    model = value;
  }

  public void setThreshold(String command, int commandId, int value) throws IOException {
    if (value < 0 || value > 100) throw new IOException("Threshold outside 0..100");
    command(command + "=" + value, commandId, 3000);
  }

  public void setKeypoints(boolean enabled) throws IOException {
    command("TKPTS=" + (enabled ? 1 : 0), 0x37, 3000);
  }

  public List<String> learnedNames(int requestedModel) throws IOException {
    if (requestedModel != 1 && requestedModel != 3) throw new IOException("Learned names require HAND or POSE model");
    Object value = command(requestedModel == 1 ? "HANDLIST?" : "POSELIST?",
        requestedModel == 1 ? 0x30 : 0x29, 3000);
    if (!(value instanceof List<?> result)) throw new IOException("Invalid learned-name response");
    List<String> names = new ArrayList<>();
    for (Object element : result) if (element instanceof String name) names.add(name);
    if (requestedModel == 1) handNames = List.copyOf(names);
    else poseNames = List.copyOf(names);
    return List.copyOf(names);
  }

  public List<Detection> invoke() throws IOException {
    detections.clear();
    eventComplete = false;
    eventStatus = 0;
    command("INVOKE=1,0,1", 0x17, 1000);
    long limit = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(1000);
    while (!eventComplete && System.nanoTime() < limit) poll();
    if (!eventComplete) throw new IOException("SEN0670 inference timed out");
    if (eventStatus != 0) throw new IOException("SEN0670 inference failed: " + eventStatus);
    List<Detection> result = new ArrayList<>();
    for (Detection detection : detections) {
      if (detection != null) {
        result.add(new Detection(detection.id(), className(detection.id()), detection.x(), detection.y(),
            detection.width(), detection.height(), detection.score(), detection.keypoints()));
      }
    }
    return List.copyOf(result);
  }

  private String className(int id) {
    if (model == 4) return id >= 0 && id < GESTURES.size() ? GESTURES.get(id) : "unknown";
    List<String> names = model == 1 ? handNames : poseNames;
    return id > 0 && id <= names.size() ? names.get(id - 1) : "unknown";
  }

  private Object command(String command, int id, int timeoutMs) throws IOException {
    responseId = -1;
    responseValue = null;
    write(("AT+" + command + "\r\n").getBytes(StandardCharsets.US_ASCII));
    long limit = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMs);
    while (responseId != id && System.nanoTime() < limit) poll();
    if (responseId != id) throw new IOException("SEN0670 command timed out: " + command);
    if (responseStatus != 0) throw new IOException("SEN0670 command rejected: " + command + " (" + responseStatus + ")");
    return responseValue;
  }

  private void write(byte[] data) throws IOException {
    for (int index = 0; index < data.length; index += TRANSFER) {
      int length = Math.min(TRANSFER, data.length - index);
      byte[] packet = new byte[length + 6];
      packet[0] = 0x10; packet[1] = 0x02;
      packet[2] = (byte) (length >>> 8); packet[3] = (byte) length;
      System.arraycopy(data, index, packet, 4, length);
      synchronized (I2CDeviceScheduler.getI2cBusLock()) {
        if (bus.write(packet, 0, packet.length) < 0) throw new IOException("SEN0670 write failed");
      }
    }
  }

  private void poll() throws IOException {
    byte[] query = {0x10, 0x03, 0, 0, 0, 0};
    byte[] available = new byte[2];
    synchronized (I2CDeviceScheduler.getI2cBusLock()) {
      if (bus.write(query, 0, query.length) < 0) throw new IOException("SEN0670 availability request failed");
      readFully(available);
    }
    int count = (available[0] & 255) << 8 | (available[1] & 255);
    if (count > FRAME_LIMIT) throw new IOException("SEN0670 response exceeds buffer limit");
    for (int remaining = count; remaining > 0;) {
      int length = Math.min(remaining, TRANSFER);
      byte[] request = {0x10, 0x01, (byte) (length >>> 8), (byte) length, 0, 0};
      synchronized (I2CDeviceScheduler.getI2cBusLock()) {
        if (bus.write(request, 0, request.length) < 0) throw new IOException("SEN0670 read request failed");
      }
      pause(10);
      byte[] data = new byte[length];
      synchronized (I2CDeviceScheduler.getI2cBusLock()) {
        readFully(data);
      }
      append(data);
      remaining -= length;
    }
    if (count == 0) pause(1);
  }

  private void readFully(byte[] result) throws IOException {
    int offset = 0;
    while (offset < result.length) {
      int count = bus.read(result, offset, result.length - offset);
      if (count <= 0) throw new IOException("Incomplete SEN0670 I2C read");
      offset += count;
    }
  }

  private static void pause(int millis) throws IOException {
    try { Thread.sleep(millis); }
    catch (InterruptedException interrupted) {
      Thread.currentThread().interrupt();
      throw new IOException("SEN0670 I2C request interrupted", interrupted);
    }
  }

  private void append(byte[] chunk) throws IOException {
    byte[] merged = Arrays.copyOf(pending, pending.length + chunk.length);
    System.arraycopy(chunk, 0, merged, pending.length, chunk.length);
    pending = merged;
    int offset = 0;
    while (offset + 11 <= pending.length) {
      if ((pending[offset] & 255) != 0x55 || (pending[offset + 1] & 255) != 0xaa) { offset++; continue; }
      int length = u16(pending, offset + 7);
      if (length + 11 > FRAME_LIMIT) { offset++; continue; }
      if (offset + length + 11 > pending.length) break;
      byte[] frame = Arrays.copyOfRange(pending, offset, offset + length + 11);
      int calculated = crc(frame, 0, 9, 0xffff);
      calculated = crc(frame, 11, length, calculated);
      if (calculated != u16(frame, 9)) { offset++; continue; }
      process(frame[3] & 255, frame[4] & 255, Arrays.copyOfRange(frame, 11, frame.length));
      offset += frame.length;
    }
    pending = Arrays.copyOfRange(pending, offset, pending.length);
    if (pending.length > FRAME_LIMIT) throw new IOException("SEN0670 frame accumulation exceeded limit");
  }

  private static int crc(byte[] bytes, int offset, int length, int crc) {
    for (int i = offset; i < offset + length; i++) {
      crc ^= (bytes[i] & 255) << 8;
      for (int bit = 0; bit < 8; bit++) {
        crc = (crc & 0x8000) == 0 ? (crc << 1) & 0xffff : ((crc << 1) ^ 0x1021) & 0xffff;
      }
    }
    return crc;
  }

  private void process(int type, int flags, byte[] data) throws IOException {
    if (type == 0x11) {
      fragments.writeBytes(data);
      if ((flags & 1) == 0) return;
      byte[] combined = fragments.toByteArray();
      fragments.reset();
      if (combined.length < 12) throw new IOException("Short binary AT response");
      responseStatus = (short) u16(combined, 2);
      responseId = u16(combined, 4);
      long length = u32(combined, 8);
      responseValue = length >= 8 && length <= combined.length - 12
          ? decode(Arrays.copyOfRange(combined, 12, 12 + (int) length)) : null;
      return;
    }
    if (type == 0) {
      if (data.length >= 4) {
        int selected = u16(data, 2);
        if (selected == 1 || selected == 3 || selected == 4) model = selected;
      }
      responseId = 0x17;
      responseStatus = data.length >= 2 ? (short) u16(data, 0) : 0;
    } else if (type == 1) {
      detections.clear();
      if (data.length >= 12) {
        int selected = u16(data, 10);
        if (selected == 1 || selected == 3 || selected == 4) model = selected;
      }
    } else if (type == 3) {
      if (data.length < 6 || (data[5] & 255) != 11) throw new IOException("Invalid detection chunk");
      int start = u16(data, 2);
      int count = data[4] & 255;
      if (6 + 11 * count > data.length) throw new IOException("Truncated detection chunk");
      for (int i = 0; i < count && start + i < RESULT_LIMIT; i++) {
        int at = 6 + 11 * i;
        Detection previous = atIndex(start + i);
        replace(start + i, new Detection(u16(data, at + 9), "unknown", u16(data, at), u16(data, at + 2),
            u16(data, at + 4), u16(data, at + 6), data[at + 8] & 255,
            previous == null ? List.of() : previous.keypoints()));
      }
    } else if (type == 4 || type == 5) {
      if (data.length < 10) throw new IOException("Short keypoint chunk");
      int index = u16(data, 2);
      if (index >= RESULT_LIMIT) return;
      int pointStart = data[5] & 255;
      int pointCount = data[6] & 255;
      int format = data[7] & 255;
      int at = 10;
      Detection previous = atIndex(index);
      if ((data[8] & 255) != 0) {
        if (at + 11 > data.length) throw new IOException("Missing keypoint bounding box");
        previous = new Detection(u16(data, at + 9), "unknown", u16(data, at), u16(data, at + 2),
            u16(data, at + 4), u16(data, at + 6), data[at + 8] & 255,
            previous == null ? List.of() : previous.keypoints());
        at += 11;
      }
      if (previous == null) previous = new Detection(0, "unknown", 0, 0, 0, 0, 0, List.of());
      List<Point> points = new ArrayList<>(previous.keypoints());
      for (int i = 0; i < pointCount; i++) {
        int width = format == 1 ? 6 : 4;
        if (at + width > data.length) throw new IOException("Truncated keypoint coordinates");
        int pointIndex = format == 1 && (data[at + 5] & 255) < 17 ? data[at + 5] & 255 : pointStart + i;
        if (pointIndex < 21) {
          while (points.size() <= pointIndex) points.add(null);
          points.set(pointIndex, new Point(u16(data, at), u16(data, at + 2)));
        }
        at += width;
      }
      replace(index, new Detection(previous.id(), previous.name(), previous.x(), previous.y(),
          previous.width(), previous.height(), previous.score(), points));
    } else if (type == 7) {
      eventStatus = data.length >= 2 ? (short) u16(data, 0) : 0;
      eventComplete = true;
    }
  }

  private Detection atIndex(int index) { return index < detections.size() ? detections.get(index) : null; }

  private void replace(int index, Detection value) {
    while (detections.size() <= index) detections.add(null);
    detections.set(index, value);
  }

  private static Object decode(byte[] value) throws IOException {
    if (value.length < 8) throw new IOException("Truncated binary value");
    int type = value[0] & 255;
    long longLength = u32(value, 4);
    if (longLength > value.length - 8) throw new IOException("Invalid binary length");
    int length = (int) longLength;
    return switch (type) {
      case 5 -> new String(value, 8, length, StandardCharsets.UTF_8);
      case 2, 3 -> length >= 8 ? (value[8] & 255) : null;
      case 1 -> (value[1] & 255) != 0 ? 1 : 0;
      case 7 -> {
        if (length < 2) throw new IOException("Truncated name array");
        int count = u16(value, 8), offset = 10;
        List<String> names = new ArrayList<>();
        for (int i = 0; i < count; i++) {
          if (offset + 8 > value.length) throw new IOException("Truncated array item");
          long itemLength = u32(value, offset + 4);
          if (itemLength > value.length - offset - 8) throw new IOException("Invalid array item");
          Object item = decode(Arrays.copyOfRange(value, offset, offset + 8 + (int) itemLength));
          if (item instanceof String name) names.add(name);
          offset += 8 + (int) itemLength;
        }
        yield List.copyOf(names);
      }
      case 6 -> {
        if (length < 2) throw new IOException("Truncated response object");
        int count = u16(value, 8), offset = 10;
        Object found = null;
        for (int i = 0; i < count; i++) {
          if (offset + 2 > value.length) throw new IOException("Truncated object key");
          int keyLength = u16(value, offset); offset += 2;
          if (keyLength > value.length - offset - 8) throw new IOException("Invalid object key");
          String key = new String(value, offset, keyLength, StandardCharsets.UTF_8);
          offset += keyLength;
          long itemLength = u32(value, offset + 4);
          if (itemLength > value.length - offset - 8) throw new IOException("Invalid object value");
          if ("id".equals(key)) found = decode(Arrays.copyOfRange(value, offset, offset + 8 + (int) itemLength));
          offset += 8 + (int) itemLength;
        }
        yield found;
      }
      default -> null;
    };
  }

  private static int u16(byte[] data, int offset) {
    return (data[offset] & 255) | ((data[offset + 1] & 255) << 8);
  }

  private static long u32(byte[] data, int offset) {
    return ((long) u16(data, offset) & 65535L) | ((long) u16(data, offset + 2) << 16);
  }
}
