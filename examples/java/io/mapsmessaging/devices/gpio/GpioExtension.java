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

package io.mapsmessaging.devices.gpio;

import io.mapsmessaging.devices.DeviceBusManager;
import io.mapsmessaging.devices.deviceinterfaces.Gpio;
import io.mapsmessaging.devices.gpio.pin.BaseDigitalOutput;
import io.mapsmessaging.devices.i2c.I2CBusManager;
import io.mapsmessaging.devices.i2c.I2CDeviceScheduler;
import io.mapsmessaging.devices.i2c.devices.gpio.mcp23017.Mcp23017Controller;
import io.mapsmessaging.devices.i2c.devices.gpio.mcp23017.Mcp23017Device;

import java.io.IOException;

public class GpioExtension {


  public static void main(String[] args) throws IOException, InterruptedException {
    I2CBusManager[] i2cBusManagers = DeviceBusManager.getInstance().getI2cBusManager();
    int bus = 1;
    if (args.length > 0) {
      bus = Integer.parseInt(args[0]);
    }
    I2CDeviceScheduler deviceController = (I2CDeviceScheduler) i2cBusManagers[bus].configureDevice(0x27, "MCP23017");
    if (deviceController.getDeviceController() instanceof Mcp23017Controller) {
      Gpio gpio = (Mcp23017Device) deviceController.getDeviceController().getDevice();
      GpioExtensionPinManagement pinManagement = new GpioExtensionPinManagement(gpio);
      BaseDigitalOutput[] outputs = new BaseDigitalOutput[gpio.getPins()];
      for (int x = 0; x < gpio.getPins(); x++) {
        outputs[x] = pinManagement.allocateOutPin("ID:" + x, "Name:" + x, x, false);
        outputs[x].setDown();
      }
      while (true) {
        for (BaseDigitalOutput out : outputs) {
          out.setUp();
          Thread.sleep(100);
        }
        Thread.sleep(2000);
        for (BaseDigitalOutput out : outputs) {
          out.setDown();
          Thread.sleep(100);
        }
        Thread.sleep(2000);
      }
    }
  }
}
