package io.mapsmessaging.devices.i2c.devices.sensors.sen0670;

import io.mapsmessaging.devices.impl.AddressableDevice;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Sen0670ProtocolTest {

  @Test
  void completedPoseBatchKeepsBoundingBoxAndKeypoints() throws Exception {
    FakeBus bus = new FakeBus();
    List<Sen0670Protocol.Detection> results = new Sen0670Protocol(bus).invoke();
    assertEquals(1, results.size());
    Sen0670Protocol.Detection result = results.getFirst();
    assertEquals(7, result.id());
    assertEquals(120, result.x());
    assertEquals(180, result.y());
    assertEquals(60, result.width());
    assertEquals(75, result.height());
    assertEquals(95, result.score());
    assertEquals(new Sen0670Protocol.Point(21, 34), result.keypoints().getFirst());
    assertThrows(UnsupportedOperationException.class, () -> result.keypoints().add(new Sen0670Protocol.Point(1, 1)));
  }

  @Test
  void datasetRetainsCompletionTimeAndRejectsMutation() {
    Sen0670DataSet result = new Sen0670DataSet(java.time.Instant.EPOCH, 3,
        List.of(new Sen0670Protocol.Detection(0, "unknown", 1, 2, 3, 4, 95, List.of())));
    assertEquals(java.time.Instant.EPOCH, result.timestamp());
    assertThrows(UnsupportedOperationException.class, () -> result.detections().clear());
  }

  private static final class FakeBus implements AddressableDevice {
    private final Queue<Byte> queued = new ArrayDeque<>();
    private boolean availability;
    private boolean reading;

    @Override public int write(byte[] bytes, int start, int length) {
      if (length >= 6 && bytes[start] == 0x10 && bytes[start + 1] == 0x03) {
        availability = true;
        reading = false;
      } else if (length >= 6 && bytes[start] == 0x10 && bytes[start + 1] == 0x01) {
        availability = false;
        reading = true;
      } else if (length > 6 && bytes[start] == 0x10 && bytes[start + 1] == 0x02) {
        String command = new String(bytes, start + 4, length - 6, StandardCharsets.US_ASCII);
        if (command.startsWith("AT+INVOKE=")) load();
      }
      return length;
    }

    private void load() {
      add(frame(0, new byte[]{0, 0, 3, 0}));
      add(frame(1, new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0}));
      add(frame(3, new byte[]{1, 0, 0, 0, 1, 11, 120, 0, (byte) 180, 0, 60, 0, 75, 0, 95, 7, 0}));
      add(frame(5, new byte[]{1, 0, 0, 0, 17, 0, 1, 1, 0, 0, 21, 0, 34, 0, 90, 0}));
      add(frame(7, new byte[]{0, 0}));
    }

    private void add(byte[] frame) {
      for (byte element : frame) queued.add(element);
    }

    private static byte[] frame(int type, byte[] data) {
      byte[] result = new byte[11 + data.length];
      result[0] = 0x55;
      result[1] = (byte) 0xaa;
      result[3] = (byte) type;
      result[4] = 1;
      result[7] = (byte) data.length;
      result[8] = (byte) (data.length >>> 8);
      System.arraycopy(data, 0, result, 11, data.length);
      int crc = 0xffff;
      for (int i = 0; i < result.length; i++) {
        if (i == 9 || i == 10) continue;
        crc ^= (result[i] & 255) << 8;
        for (int bit = 0; bit < 8; bit++) {
          crc = (crc & 0x8000) != 0 ? (crc << 1 ^ 0x1021) & 0xffff : crc << 1 & 0xffff;
        }
      }
      result[9] = (byte) crc;
      result[10] = (byte) (crc >>> 8);
      return result;
    }

    @Override public int read(byte[] bytes, int offset, int length) {
      if (availability) {
        bytes[offset] = (byte) (queued.size() >>> 8);
        bytes[offset + 1] = (byte) queued.size();
        availability = false;
        return 2;
      }
      if (!reading || queued.isEmpty()) return -1;
      int size = Math.min(length, queued.size());
      for (int i = 0; i < size; i++) bytes[offset + i] = queued.remove();
      return size;
    }

    @Override public int getBus() { return 1; }
    @Override public int getDevice() { return 0x3a; }
    @Override public int write(int val) { return 1; }
    @Override public int writeRegister(int address, byte[] data) { return data.length; }
    @Override public int readRegister(int address) { return 0; }
    @Override public int readRegister(int address, byte[] data, int offset, int length) { return -1; }
    @Override public int read() { return -1; }
    @Override public void close() {}
  }
}
