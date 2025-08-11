/*
 *    Copyright [ 2020 - 2024 ] Matthew Buckton
 *    Copyright [ 2024 - 2025 ] MapsMessaging B.V.
 *
 *    Licensed under the Apache License, Version 2.0 with the Commons Clause
 *    (the "License"); you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at:
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *        https://commonsclause.com/
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License
 */

package io.mapsmessaging.devices.serial.devices.sensors.sen0642;

import io.mapsmessaging.devices.Device;
import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.sensorreadings.FloatSensorReading;
import io.mapsmessaging.devices.sensorreadings.IntegerSensorReading;
import io.mapsmessaging.devices.sensorreadings.SensorReading;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.util.List;

public class Sen0642Sensor implements Device, Sensor {

  private static final byte[] READ_UV = new byte[]{0x01, 0x03, 0x00, 0x00, 0x00, 0x01, (byte) 0x84, 0x0A};
  private static final byte[] READ_UVI = new byte[]{0x01, 0x03, 0x00, 0x01, 0x00, 0x01, (byte) 0xD5, (byte) 0xCA};

  private static final int RESP_LEN = 7; // 01 03 02 HI LO CRC_H CRC_L

  private final InputStream in;
  private final OutputStream out;

  private final List<SensorReading<?>> readings;

  private volatile float lastUv_mWcm2 = Float.NaN;
  private volatile int lastUvi = -1;

  public Sen0642Sensor(InputStream in, OutputStream out) {
    this.in = in;
    this.out = out;
    this.readings = List.of(
        new FloatSensorReading(
            "uv", "mW/cm²", "Ultraviolet irradiance (290–390nm, ±10%FS @365nm, 25°C, 60%RH)",
            1.00f,             // example value
            true,              // readOnly
            0.00f,             // min
            15.00f,            // max
            2,                 // precision (0.01 mW/cm² resolution)
            this::getUvMilliWPerCm2
        ),
        new IntegerSensorReading(
            "uvi", "", "Ultraviolet index (0–15, resolution 1)",
            1,                 // example value
            true,              // readOnly
            0,                 // min
            15,                // max
            this::getUvi
        ));
  }

  private static int crc16(byte[] buf, int len) {
    int crc = 0xFFFF;
    for (int i = 0; i < len; i++) {
      crc ^= (buf[i] & 0xFF);
      for (int b = 0; b < 8; b++) {
        if ((crc & 1) != 0) crc = (crc >>> 1) ^ 0xA001;
        else crc >>>= 1;
      }
    }
    // swap like the Arduino example
    return ((crc & 0x00FF) << 8) | ((crc & 0xFF00) >>> 8);
  }

  public synchronized float getUvMilliWPerCm2() throws IOException {
    lastUv_mWcm2 = readRegisterScaled(READ_UV, 1f / 100f);
    return lastUv_mWcm2;
  }

  public synchronized int getUvi() throws IOException {
    lastUvi = Math.round(readRegisterScaled(READ_UVI, 1f));
    return lastUvi;
  }

  private float readRegisterScaled(byte[] cmd, float scale) throws IOException {
    write(cmd);
    byte[] resp = readExact(RESP_LEN, Duration.ofMillis(500));
    // basic frame check
    if (resp[0] != 0x01 || resp[1] != 0x03 || resp[2] != 0x02) {
      throw new IOException("Bad frame");
    }
    if (!crcOk(resp)) {
      throw new IOException("CRC error");
    }
    int raw = ((resp[3] & 0xFF) << 8) | (resp[4] & 0xFF);
    return raw * scale;
  }

  private void write(byte[] buf) throws IOException {
    out.write(buf);
    out.flush();
  }

  private byte[] readExact(int n, Duration timeout) throws IOException {
    byte[] b = new byte[n];
    int off = 0;
    long deadline = System.nanoTime() + timeout.toNanos();
    while (off < n) {
      int available = in.available();
      if (available <= 0) {
        if (System.nanoTime() > deadline) break;
        try {
          Thread.sleep(2);
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
        }
        continue;
      }
      int r = in.read(b, off, n - off);
      if (r < 0) break;
      off += r;
    }
    if (off != n) throw new IOException("Timeout");
    return b;
  }

  private boolean crcOk(byte[] resp) {
    int calc = crc16(resp, 5); // first 5 bytes
    int got = ((resp[5] & 0xFF) << 8) | (resp[6] & 0xFF);
    return calc == got;
  }

  @Override
  public String getName() {
    return "SEN0642";
  }

  @Override
  public String getDescription() {
    return "DFRobot SEN0642 UV sensor (UART/Modbus)";
  }

  @Override
  public DeviceType getType() {
    return DeviceType.SENSOR;
  }

  @Override
  public List<SensorReading<?>> getReadings() {
    return readings;
  }
}