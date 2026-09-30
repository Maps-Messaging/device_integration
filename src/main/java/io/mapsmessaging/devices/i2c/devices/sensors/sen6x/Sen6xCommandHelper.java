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

package io.mapsmessaging.devices.i2c.devices.sensors.sen6x;

import com.pi4j.exception.Pi4JException;
import io.mapsmessaging.devices.impl.AddressableDevice;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class Sen6xCommandHelper {

  private static final int CRC8_POLY = 0x31;
  private static final int CRC8_INIT = 0xFF;
  private final AddressableDevice device;

  public Sen6xCommandHelper(AddressableDevice device) {
    this.device = Objects.requireNonNull(device, "device");
  }

  public byte[] requestResponse(int command, int expectedResponseLength) throws IOException {
    return requestResponse(command, expectedResponseLength, 20);
  }

  public String requestAsciiResponse(int command, int expectedResponseLength) throws IOException {
    return requestAsciiResponse(command, expectedResponseLength, 20);
  }

  public String requestAsciiResponse(int command, int expectedResponseLength, int delayMillis) throws IOException {
    byte[] raw = requestResponse(command, expectedResponseLength, delayMillis);
    int end = 0;
    while (end < raw.length && raw[end] != 0) {
      end++;
    }
    return new String(raw, 0, end, StandardCharsets.US_ASCII).trim();
  }

  public void sendCommand(int commandId) throws IOException {
    requestResponse(commandId, 0, 20);
  }

  /** Writes a two-byte command followed by data words; CRC applies only to data words. */
  public void writeWithCRC(byte[] data) {
    if (data.length < 2 || data.length % 2 != 0) {
      throw new IllegalArgumentException("SEN6x writes require a command and complete two-byte data words");
    }
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    out.write(data[0]);
    out.write(data[1]);
    for (int index = 2; index < data.length; index += 2) {
      out.write(data[index]);
      out.write(data[index + 1]);
      out.write(computeCRC(data[index], data[index + 1]));
    }
    synchronized (device) {
      try {
        checkInterrupted();
        writeChecked(out.toByteArray());
        delay(20);
        checkInterrupted();
      } catch (IOException exception) {
        throw new UncheckedIOException(exception);
      }
    }
  }

  public byte[] requestResponse(int command, int expectedResponseLength, int delayMillis) throws IOException {
    if (command < 0 || command > 0xffff || expectedResponseLength < 0
        || expectedResponseLength % 3 != 0 || delayMillis < 0) {
      throw new IllegalArgumentException("Invalid SEN6x command, response length or delay");
    }
    synchronized (device) {
      checkInterrupted();
      writeChecked(new byte[] {(byte) (command >>> 8), (byte) command});
      delay(delayMillis);
      checkInterrupted();
      if (expectedResponseLength == 0) {
        return new byte[0];
      }
      byte[] response = new byte[expectedResponseLength];
      int read;
      try {
        read = device.read(response, 0, response.length);
      } catch (Pi4JException exception) {
        throw new IOException("Unable to read SEN6x response", exception);
      }
      if (read != expectedResponseLength) {
        throw new IOException("Incomplete SEN6x response: expected " + expectedResponseLength + " bytes, read " + read);
      }
      return decodeRawData(response);
    }
  }

  private void writeChecked(byte[] data) throws IOException {
    int written;
    try {
      written = device.write(data);
    } catch (Pi4JException exception) {
      throw new IOException("Unable to write SEN6x command", exception);
    }
    if (written != data.length) {
      throw new IOException("Incomplete SEN6x write: expected " + data.length + " bytes, wrote " + written);
    }
  }

  private byte[] decodeRawData(byte[] raw) throws IOException {
    byte[] result = new byte[raw.length / 3 * 2];
    for (int offset = 0; offset < raw.length; offset += 3) {
      byte msb = raw[offset];
      byte lsb = raw[offset + 1];
      if (raw[offset + 2] != computeCRC(msb, lsb)) {
        throw new IOException("SEN6x CRC mismatch at word " + offset / 3);
      }
      result[offset / 3 * 2] = msb;
      result[offset / 3 * 2 + 1] = lsb;
    }
    return result;
  }

  private byte computeCRC(byte msb, byte lsb) {
    int crc = CRC8_INIT;
    for (byte value : new byte[] {msb, lsb}) {
      crc ^= value & 0xff;
      for (int bit = 0; bit < 8; bit++) {
        crc = ((crc & 0x80) != 0 ? (crc << 1) ^ CRC8_POLY : crc << 1) & 0xff;
      }
    }
    return (byte) crc;
  }

  private void checkInterrupted() throws InterruptedIOException {
    if (Thread.currentThread().isInterrupted()) {
      throw new InterruptedIOException("Interrupted during SEN6x command");
    }
  }

  public void delay(int delayMs) {
    if (delayMs <= 0) {
      return;
    }
    try {
      Thread.sleep(delayMs);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
    }
  }
}
