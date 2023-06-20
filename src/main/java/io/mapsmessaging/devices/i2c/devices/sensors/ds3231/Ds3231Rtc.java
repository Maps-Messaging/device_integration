/*
 *      Copyright [ 2020 - 2023 ] [Matthew Buckton]
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package io.mapsmessaging.devices.i2c.devices.sensors.ds3231;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.logging.Logger;
import io.mapsmessaging.logging.LoggerFactory;

public class Ds3231Rtc extends I2CDevice {

  private final Logger logger = LoggerFactory.getLogger(Ds3231Rtc.class);

  public Ds3231Rtc(I2C device) {
    super(device);
    initialise();
  }

  @Override
  public boolean isConnected() {
    return true;
  }

  public synchronized boolean initialise() {
    return true;
  }

  private synchronized void scanForChange() {
  }

  @Override
  public String getName() {
    return "DS3231";
  }

  @Override
  public String getDescription() {
    return "Real Time Clock";
  }
}