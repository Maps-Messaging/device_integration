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

package io.mapsmessaging.devices.i2c.devices.sensors.sht31.commands;

import com.pi4j.exception.Pi4JException;
import io.mapsmessaging.devices.i2c.devices.sensors.sht31.Sht31Sensor;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.UncheckedIOException;
import io.mapsmessaging.devices.impl.AddressableDevice;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public abstract class Command {

  private final int cmd;
  private final int delayTime;
  private final int responseSize;

  public byte[] sendCommand(Sht31Sensor sensor, AddressableDevice device) {
    try {
      checkInterrupted();
      byte[] commandBytes = new byte[] {(byte) (cmd >>> 8), (byte) cmd};
      int written = device.write(commandBytes);
      if (written != commandBytes.length) {
        throw new IOException("Incomplete SHT31 write: expected 2 bytes, wrote " + written);
      }
      if (delayTime > 0 && sensor != null) {
        sensor.delay(delayTime);
      }
      checkInterrupted();
      byte[] responseBytes = new byte[responseSize];
      if (responseSize > 0) {
        int count = device.read(responseBytes, 0, responseSize);
        if (count != responseSize) {
          throw new IOException("Incomplete SHT31 response: expected " + responseSize + " bytes, read " + count);
        }
        for (int index = 0; index + 2 < responseSize; index += 3) {
          if (!Crc8.check(responseBytes, index)) {
            throw new IOException("SHT31 CRC check failed at block starting index " + index);
          }
        }
      }
      return responseBytes;
    } catch (Pi4JException exception) {
      throw new UncheckedIOException("SHT31 command failed", new IOException(exception));
    } catch (IOException exception) {
      throw new UncheckedIOException(exception);
    }
  }

  private void checkInterrupted() throws InterruptedIOException {
    if (Thread.currentThread().isInterrupted()) {
      throw new InterruptedIOException("Interrupted during SHT31 command");
    }
  }
}
