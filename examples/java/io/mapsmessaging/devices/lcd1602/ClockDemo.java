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

package io.mapsmessaging.devices.lcd1602;

import io.mapsmessaging.devices.DeviceBusManager;
import io.mapsmessaging.devices.i2c.I2CBusManager;
import io.mapsmessaging.devices.i2c.I2CDeviceScheduler;
import io.mapsmessaging.devices.i2c.devices.output.lcd.lcd1602.Lcd1602Controller;
import io.mapsmessaging.devices.i2c.devices.output.lcd.lcd1602.task.Clock;

import java.io.IOException;

public class ClockDemo {

  public static void main(String[] args) throws IOException {
    I2CBusManager[] i2cBusManagers = DeviceBusManager.getInstance().getI2cBusManager();
    int bus = 1;
    if (args.length > 0) {
      bus = Integer.parseInt(args[0]);
    }
    // Configure and mount a device on address 0x5D as a LPS25 pressure & temperature
    I2CDeviceScheduler deviceController = (I2CDeviceScheduler) i2cBusManagers[bus].configureDevice(0x3e, "LCD1602");
    if (deviceController.getDeviceController() instanceof Lcd1602Controller) {
      Clock clock = new Clock((Lcd1602Controller) deviceController.getDeviceController());
      clock.run();
    }
  }

}