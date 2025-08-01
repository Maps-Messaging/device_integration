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

import java.io.IOException;

public class ReadDataCommand extends Command {
  private float lastTemperature;
  private float lastHumidity;
  private long lastReadTime = 0;
  private final long minimumReadIntervalMillis;

  public ReadDataCommand(Repeatability repeatability) {
    super(0xE000, 0, 6);
    this.minimumReadIntervalMillis = switch (repeatability) {
      case HIGH -> 15;
      case MEDIUM -> 6;
      case LOW -> 4;
    };
  }

  public synchronized void read(AddressableDevice device) throws IOException {
    long now = System.currentTimeMillis();
    if ((now - lastReadTime) < minimumReadIntervalMillis) {
      return;
    }

    byte[] response = sendCommand(device);

    int rawTemp = ((response[0] & 0xFF) << 8) | (response[1] & 0xFF);
    int rawHumidity = ((response[3] & 0xFF) << 8) | (response[4] & 0xFF);

    lastTemperature = -45 + 175 * (rawTemp / 65535.0f);
    lastHumidity = 100 * (rawHumidity / 65535.0f);
    lastReadTime = now;
  }

  public float getTemperature() {
    return lastTemperature;
  }

  public float getHumidity() {
    return lastHumidity;
  }
}
