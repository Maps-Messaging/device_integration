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

package io.mapsmessaging.devices.st7735;

import io.mapsmessaging.devices.DeviceBusManager;
import io.mapsmessaging.devices.i2c.I2CBusManager;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.devices.output.lcd.st7735.St7735Device;

import java.io.IOException;
import java.util.Random;

public class St7735Example {
  public static void main(String[] args) throws IOException {
    I2CBusManager[] i2cBusManagers = DeviceBusManager.getInstance().getI2cBusManager();
    int bus = 1;
    if (args.length > 0) {
      bus = Integer.parseInt(args[0]);
    }
    // Configure and mount a device on address 0x5D as a LPS25 pressure & temperature
    I2CDeviceController deviceController = i2cBusManagers[bus].configureDevice(0x18, "ST7735");
    if (deviceController != null) {
      System.err.println(new String(deviceController.getDeviceConfiguration()));
      I2CDevice sensor = deviceController.getDevice();
      Random random = new Random(System.currentTimeMillis());

      long exitTIme = System.currentTimeMillis() + 60000;
      if (sensor instanceof St7735Device) {
        St7735Device device1 = (St7735Device) sensor;
        device1.reset();
        while (exitTIme > System.currentTimeMillis()) {
          device1.lcdDisplayPercentage("Test:", Math.abs(random.nextInt(100)), 100);
          device1.delay(1000);
        }
      }
    }
  }
}
