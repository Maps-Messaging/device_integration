/*
 *
 *  Copyright [ 2020 - 2024 ] Matthew Buckton
 *  Copyright [ 2024 - 2026 ] MapsMessaging B.V.
 *
 *  Licensed under the Apache License, Version 2.0 with the Commons Clause
 *  (the "License"); you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      https://commonsclause.com/
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */

package io.mapsmessaging.devices.serial.devices.sensors.sen0547;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.mapsmessaging.devices.serial.devices.sensors.SerialDevice;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class Stl19pSensorTest {

  @Test
  void testProtocolCrc() {
    byte[] data = "123456789".getBytes(StandardCharsets.US_ASCII);
    assertEquals(0xC3, Stl19pSensor.crc8(data, data.length));
  }

  @Test
  void testCompleteScanAssembly() throws Exception {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    stream.writeBytes(buildFrame(35000, 35900, 1000, 1000));
    stream.writeBytes(buildFrame(100, 1100, 1100, 1100));
    stream.writeBytes(buildFrame(35000, 35900, 1200, 1200));
    stream.writeBytes(buildFrame(100, 1100, 1300, 1300));

    Stl19pSensor sensor = new Stl19pSensor(new TestSerialDevice(stream.toByteArray()));
    try {
      waitFor(() -> sensor.hasData());
      Stl19pSensor.Scan scan = sensor.getData();
      assertTrue(scan != null);

      Instant.parse(scan.getTimestamp());
      assertEquals(12, scan.getPoints().size());
      assertEquals(350.0, scan.getPoints().get(0).getAngleDegrees(), 0.001);
      assertEquals(1000, scan.getPoints().get(0).getDistanceMm());
      assertEquals(359.0, scan.getPoints().get(scan.getPoints().size() - 1).getAngleDegrees(), 0.001);
      assertEquals(1011, scan.getPoints().get(scan.getPoints().size() - 1).getDistanceMm());
    } finally {
      sensor.close();
    }
  }

  @Test
  void testScanQueueDropsOldestWhenFull() throws Exception {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    stream.writeBytes(buildFrame(35000, 35900, 100, 900));
    stream.writeBytes(buildFrame(100, 1100, 200, 950));
    for (int index = 0; index < 25; index++) {
      stream.writeBytes(buildFrame(35000, 35900, 1000 + (index * 100), 1000 + index));
      stream.writeBytes(buildFrame(100, 1100, 2000 + (index * 100), 2000 + index));
    }

    TestSerialDevice serialDevice = new TestSerialDevice(stream.toByteArray());
    Stl19pSensor sensor = new Stl19pSensor(serialDevice);
    try {
      long deadline = System.nanoTime() + Duration.ofSeconds(1).toNanos();
      while (!serialDevice.isExhausted() && System.nanoTime() < deadline) {
        Thread.sleep(1);
      }

      assertTrue(serialDevice.isExhausted());
      assertEquals(Stl19pSensor.MAX_QUEUED_SCANS, sensor.getQueuedScanCount());
      assertTrue(sensor.hasOverrun());
      assertTrue(!sensor.hasOverrun());
      Stl19pSensor.Scan oldestRetained = sensor.getData();
      assertTrue(oldestRetained != null);
      assertEquals(2500, oldestRetained.getPoints().get(0).getDistanceMm());
    } finally {
      sensor.close();
    }
  }

  @Test
  void testRawAndComputedDeviceRegistration() throws Exception {
    Stl19pController controller = new Stl19pController(new TestSerialDevice(new byte[0]));
    try {
      assertEquals(2, controller.getComputedDevices().size());
      assertEquals("SEN0547_detailed", controller.getComputedDevices().get(0).getName());
      assertEquals("SEN0547_change", controller.getComputedDevices().get(1).getName());
      assertTrue(controller.getSchema() != null);
      assertTrue(controller.getConfigEventDescription().length > 0);
      assertThrows(java.io.IOException.class, () -> controller.updateDeviceConfiguration(
          "{\"command\":\"SET_SCAN_CONFIGURATION\",\"scanMode\":\"merged\",\"mergeScans\":2}".getBytes(StandardCharsets.UTF_8)));
      Stl19pChangeController changes = (Stl19pChangeController) controller.getComputedDevices().get(1);
      byte[] result = changes.updateDeviceConfiguration(
          "{\"command\":\"SET_CHANGE_THRESHOLD\",\"thresholdMm\":200}".getBytes(StandardCharsets.UTF_8));
      assertTrue(new String(result, StandardCharsets.UTF_8).contains("success"));
    } finally {
      controller.close();
    }
  }

  @Test
  void testSevenRevolutionDetailedRecordsAndDistanceChanges() throws Exception {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    for (int revolution = 0; revolution < 16; revolution++) {
      int distance = revolution < 8 ? 1000 : 1300;
      stream.writeBytes(buildFrame(35000, 35900, distance, 1000 + revolution * 2));
      stream.writeBytes(buildFrame(100, 1100, distance, 1001 + revolution * 2));
    }
    TestSerialDevice serial = new TestSerialDevice(stream.toByteArray(), true);
    Stl19pController controller = new Stl19pController(serial);
    try {
      Stl19pDetailedController detailed = (Stl19pDetailedController) controller.getComputedDevices().get(0);
      Stl19pChangeController changes = (Stl19pChangeController) controller.getComputedDevices().get(1);
      assertTrue(detailed.getSchema() != null);
      serial.release();
      waitFor(serial::isExhausted);

      JsonObject raw = JsonParser.parseString(new String(controller.getDeviceState(), StandardCharsets.UTF_8)).getAsJsonObject();
      assertEquals(12, raw.getAsJsonArray("points").size());
      JsonObject rawPoint = raw.getAsJsonArray("points").get(0).getAsJsonObject();
      assertTrue(rawPoint.has("angleDegrees"));
      assertTrue(rawPoint.has("distanceMm"));
      assertTrue(rawPoint.has("intensity"));

      JsonObject first = JsonParser.parseString(new String(detailed.getDeviceState(), StandardCharsets.UTF_8)).getAsJsonObject();
      JsonObject second = JsonParser.parseString(new String(detailed.getDeviceState(), StandardCharsets.UTF_8)).getAsJsonObject();
      assertTrue(first.get("hasData").getAsBoolean());
      assertTrue(second.get("hasData").getAsBoolean());
      assertEquals(168, first.getAsJsonArray("points").size());
      assertEquals(168, second.getAsJsonArray("points").size());
      double prior = -1;
      for (com.google.gson.JsonElement item : first.getAsJsonArray("points")) {
        JsonObject point = item.getAsJsonObject();
        double angle = point.get("angle").getAsDouble();
        assertTrue(angle >= prior);
        prior = angle;
        assertTrue(point.has("distance"));
        assertTrue(point.has("intensity"));
        assertFalse(point.has("distanceMm"));
      }
      assertFalse(JsonParser.parseString(new String(detailed.getDeviceState(), StandardCharsets.UTF_8))
          .getAsJsonObject().get("hasData").getAsBoolean());

      JsonObject change = JsonParser.parseString(new String(changes.getDeviceState(), StandardCharsets.UTF_8)).getAsJsonObject();
      assertTrue(change.get("hasData").getAsBoolean());
      JsonObject entry = change.getAsJsonObject("change").getAsJsonArray("changes").get(0).getAsJsonObject();
      assertEquals(300, entry.get("delta").getAsInt());
      assertTrue(entry.has("intensity"));
      assertTrue(entry.has("angle"));
      assertEquals(1300, entry.get("distance").getAsInt());
    } finally {
      controller.close();
    }
  }

  @Test
  void testEmptyFifoReadReturnsImmediately() throws Exception {
    Stl19pSensor sensor = new Stl19pSensor(new TestSerialDevice(new byte[0]));
    try {
      assertTrue(!sensor.hasData());
      assertEquals(null, sensor.getData());
      assertEquals(0, sensor.queueDepth());
    } finally {
      sensor.close();
    }
  }


  private static void waitFor(java.util.function.BooleanSupplier condition) throws Exception {
    long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(1);
    while (!condition.getAsBoolean() && System.nanoTime() < deadline) {
      Thread.sleep(1);
    }
    assertTrue(condition.getAsBoolean(), "Timed out waiting for sensor reader");
  }

  private static byte[] buildFrame(int startAngle, int endAngle, int distanceBase, int timestamp) {
    byte[] frame = new byte[Stl19pSensor.FRAME_LENGTH];
    frame[0] = (byte) Stl19pSensor.FRAME_HEADER;
    frame[1] = (byte) Stl19pSensor.FRAME_VER_LEN;
    writeUnsignedShortLittleEndian(frame, 2, 3600);
    writeUnsignedShortLittleEndian(frame, 4, startAngle);

    for (int index = 0; index < Stl19pSensor.POINTS_PER_FRAME; index++) {
      int offset = 6 + (index * 3);
      writeUnsignedShortLittleEndian(frame, offset, distanceBase + index);
      frame[offset + 2] = (byte) (100 + index);
    }

    writeUnsignedShortLittleEndian(frame, 42, endAngle);
    writeUnsignedShortLittleEndian(frame, 44, timestamp);
    frame[46] = (byte) Stl19pSensor.crc8(frame, 46);
    return frame;
  }

  private static void writeUnsignedShortLittleEndian(byte[] data, int offset, int value) {
    data[offset] = (byte) (value & 0xFF);
    data[offset + 1] = (byte) ((value >> 8) & 0xFF);
  }

  private static final class TestSerialDevice implements SerialDevice {
    private final byte[] data;
    private int offset;
    private boolean open;
    private volatile boolean exhausted;
    private volatile boolean blocked;

    private TestSerialDevice(byte[] data) {
      this(data, false);
    }

    private TestSerialDevice(byte[] data, boolean blocked) {
      this.data = data;
      this.blocked = blocked;
    }

    private void release() { blocked = false; }

    @Override
    public boolean isOpen() {
      return open;
    }

    @Override
    public boolean openPort() {
      open = true;
      return true;
    }

    @Override
    public String getSystemPortName() {
      return "test";
    }

    @Override
    public void closePort() {
      open = false;
    }

    @Override
    public int writeBytes(byte[] request, int length) {
      return 0;
    }

    @Override
    public int readBytes(byte[] buffer, int remaining) {
      if (blocked) return 0;
      if (offset >= data.length) {
        exhausted = true;
        return 0;
      }
      int length = Math.min(remaining, data.length - offset);
      System.arraycopy(data, offset, buffer, 0, length);
      offset += length;
      return length;
    }

    private boolean isExhausted() {
      return exhausted;
    }
  }
}
