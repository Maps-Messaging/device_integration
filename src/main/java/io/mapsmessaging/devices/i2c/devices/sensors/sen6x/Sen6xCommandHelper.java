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

package io.mapsmessaging.devices.i2c.devices.sensors.sen6x;


import io.mapsmessaging.devices.impl.AddressableDevice;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Sen6xCommandHelper {

  private static final int CRC8_POLY = 0x31;
  private static final int CRC8_INIT = 0xFF;

  private final AddressableDevice device;

  public Sen6xCommandHelper(AddressableDevice device) {
    this.device = device;
  }

  public byte[] requestResponse(int command, int expectedResponseLength) throws IOException {
    return requestResponse(command, expectedResponseLength, 20);
  }

  public String requestAsciiResponse(int command, int expectedResponseLength) throws IOException {
    return requestAsciiResponse(command, expectedResponseLength, 20);
  }

  public String requestAsciiResponse(int command, int expectedResponseLength, int delayMillis) throws IOException {
    byte[] raw = requestResponse(command, expectedResponseLength, delayMillis);
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < raw.length; i += 2) {
      char c = (char) ((raw[i] << 8) | (raw[i + 1] & 0xFF));
      if (c != 0) sb.append(c);
    }
    return sb.toString().trim();
  }

  public void sendCommand(int commandId) throws IOException {
    byte[] cmd = new byte[2];
    cmd[0] = (byte) ((commandId >> 8) & 0xFF);
    cmd[1] = (byte) (commandId & 0xFF);
    writeWithCRC(cmd);
  }

  public void writeWithCRC(byte[] data) throws IOException {
    if (data.length % 2 != 0) {
      throw new IllegalArgumentException("Data length must be even to compute CRC per 2-byte word.");
    }

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    for (int i = 0; i < data.length; i += 2) {
      out.write(data[i]);
      out.write(data[i + 1]);
      out.write(computeCRC(data[i], data[i + 1]));
    }

    device.write(out.toByteArray());
  }
  public byte[] requestResponse(int command, int expectedResponseLength, int delayMillis) throws IOException {
    byte[] commandBytes = new byte[] {
        (byte) ((command >> 8) & 0xFF),
        (byte) (command & 0xFF)
    };
    device.write(commandBytes);

    // Delay if needed; typically 20ms for most read ops
    delay(delayMillis);
    byte[] response = new byte[expectedResponseLength];
    int read = device.read(response, 0, response.length);
    byte[] actualResponse = new byte[read];
    System.arraycopy(response, 0, actualResponse, 0, read);
    return decodeRawData(actualResponse);
  }

  private byte[] decodeRawData(byte[] raw) throws IOException {
    int count = raw.length / 3;
    byte[] result = new byte[count * 2];

    for (int i = 0; i < count; i++) {
      int offset = i * 3;
      byte msb = raw[offset];
      byte lsb = raw[offset + 1];
      byte crc = raw[offset + 2];

      if (crc != computeCRC(msb, lsb)) {
        throw new IOException("CRC mismatch at word " + i);
      }

      result[i * 2] = msb;
      result[i * 2 + 1] = lsb;
    }

    return result;
  }

  private byte computeCRC(byte msb, byte lsb) {
    byte[] data = { msb, lsb };
    byte crc = (byte) CRC8_INIT;

    for (byte b : data) {
      crc ^= b;
      for (int i = 0; i < 8; i++) {
        if ((crc & 0x80) != 0) {
          crc = (byte) ((crc << 1) ^ CRC8_POLY);
        } else {
          crc <<= 1;
        }
      }
    }

    return crc;
  }

  public void delay(int delayMs) {
    try {
      device.wait(delayMs);
    } catch (InterruptedException e) {
    }
  }
}
