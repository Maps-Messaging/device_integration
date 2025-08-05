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

import io.mapsmessaging.devices.i2c.devices.sensors.sht31.Sht31Sensor;
import io.mapsmessaging.devices.impl.AddressableDevice;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class ReadDataCommand extends Command {
  private float lastTemperature;
  private float lastHumidity;
  private long lastReadTime = 0;
  private final int minimumReadIntervalMillis;
  private final int nextReadingIntervalMillis;

  public ReadDataCommand(Repeatability repeatability, Mps mps) {
    super(0xE000, 0, 6);
    this.minimumReadIntervalMillis = switch (repeatability) {
      case HIGH -> 20;
      case MEDIUM -> 10;
      case LOW -> 8;
    };
    nextReadingIntervalMillis = mps.getIntervalMillis();
  }

  public synchronized void read(Sht31Sensor sensor,  AddressableDevice device) {
    long now = System.currentTimeMillis();
    if ((now - lastReadTime) < nextReadingIntervalMillis) {
      return;
    }

    byte[] response = sendCommand(sensor, device);
    sensor.delay(minimumReadIntervalMillis);

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
