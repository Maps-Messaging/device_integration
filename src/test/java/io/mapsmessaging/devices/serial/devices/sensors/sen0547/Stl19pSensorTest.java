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

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.mapsmessaging.devices.serial.devices.sensors.SerialDevice;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class Stl19pSensorTest {

  @Test
  void testProtocolCrc() {
    byte[] data = "123456789".getBytes(StandardCharsets.US_ASCII);
    assertEquals(0xC3, Stl19pSensor.crc8(data, data.length));
  }

  @Test
  void testCompleteScanAssembly() throws IOException {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    stream.writeBytes(buildFrame(35000, 35900, 1000));
    stream.writeBytes(buildFrame(100, 1100, 1100));
    stream.writeBytes(buildFrame(35000, 35900, 1200));
    stream.writeBytes(buildFrame(100, 1100, 1300));

    Stl19pSensor sensor = new Stl19pSensor(new TestSerialDevice(stream.toByteArray()));
    try {
      Stl19pSensor.Scan scan = sensor.readScan();

      assertEquals(10.0, scan.getScanFrequencyHz(), 0.001);
      assertEquals(1200, scan.getSensorTimestampMs());
      assertEquals(24, scan.getPoints().size());
      assertEquals(1.0, scan.getPoints().get(0).getAngleDegrees(), 0.001);
      assertEquals(359.0, scan.getPoints().get(scan.getPoints().size() - 1).getAngleDegrees(), 0.001);
    } finally {
      sensor.close();
    }
  }

  @Test
  void testScanQueueDropsOldestWhenFull() throws Exception {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    stream.writeBytes(buildFrame(35000, 35900, 900));
    stream.writeBytes(buildFrame(100, 1100, 950));
    for (int index = 0; index < 25; index++) {
      stream.writeBytes(buildFrame(35000, 35900, 1000 + index));
      stream.writeBytes(buildFrame(100, 1100, 2000 + index));
    }

    Stl19pSensor sensor = new Stl19pSensor(new TestSerialDevice(stream.toByteArray()));
    sensor.setResponseTimeout(Duration.ofSeconds(1));
    try {
      long deadline = System.nanoTime() + Duration.ofSeconds(1).toNanos();
      while (sensor.getQueuedScanCount() < Stl19pSensor.MAX_QUEUED_SCANS && System.nanoTime() < deadline) {
        Thread.sleep(1);
      }

      assertEquals(Stl19pSensor.MAX_QUEUED_SCANS, sensor.getQueuedScanCount());
      Stl19pSensor.Scan oldestRetained = sensor.readScan();
      assertEquals(1005, oldestRetained.getSensorTimestampMs());
    } finally {
      sensor.close();
    }
  }

  private static byte[] buildFrame(int startAngle, int endAngle, int timestamp) {
    byte[] frame = new byte[Stl19pSensor.FRAME_LENGTH];
    frame[0] = (byte) Stl19pSensor.FRAME_HEADER;
    frame[1] = (byte) Stl19pSensor.FRAME_VER_LEN;
    writeUnsignedShortLittleEndian(frame, 2, 3600);
    writeUnsignedShortLittleEndian(frame, 4, startAngle);

    for (int index = 0; index < Stl19pSensor.POINTS_PER_FRAME; index++) {
      int offset = 6 + (index * 3);
      writeUnsignedShortLittleEndian(frame, offset, 1000 + index);
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

    private TestSerialDevice(byte[] data) {
      this.data = data;
    }

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
      if (offset >= data.length) {
        return 0;
      }
      int length = Math.min(remaining, data.length - offset);
      System.arraycopy(data, offset, buffer, 0, length);
      offset += length;
      return length;
    }
  }
}
