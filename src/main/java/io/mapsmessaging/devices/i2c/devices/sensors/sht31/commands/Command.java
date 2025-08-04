/*
 *
 *  Copyright [ 2020 - 2024 ] Matthew Buckton
 *  Copyright [ 2024 - 2025 ] MapsMessaging B.V.
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

import io.mapsmessaging.devices.impl.AddressableDevice;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.IOException;

@Data
@AllArgsConstructor
public abstract class Command {

  private final int cmd;
  private final long delayTime;
  private final int responseSize;

  public byte[] sendCommand(AddressableDevice device) throws IOException {
    byte[] commandBytes = new byte[2];
    commandBytes[0] = (byte) ((cmd >> 8) & 0xFF); // MSB
    commandBytes[1] = (byte) (cmd & 0xFF);        // LSB
    device.write(commandBytes);
    if(delayTime > 0){
      synchronized(device){
        try {
          device.wait(delayTime);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new IOException("Sensor read was interrupted");
        }
      }
    }
    byte[] responseBytes = new byte[responseSize];
    if(responseSize > 0){
      device.read(responseBytes, 0, responseSize);
      // Validate each 2-byte block + CRC
      for (int i = 0; i + 2 < responseSize; i += 3) {
        if (!Crc8.check(responseBytes, i)) {
          throw new IllegalStateException("CRC check failed at block starting index " + i);
        }
      }
    }
    return responseBytes;
  }
}
