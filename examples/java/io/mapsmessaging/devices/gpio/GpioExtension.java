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

package io.mapsmessaging.devices.gpio;

import io.mapsmessaging.devices.DeviceBusManager;
import io.mapsmessaging.devices.deviceinterfaces.Gpio;
import io.mapsmessaging.devices.gpio.pin.BaseDigitalInput;
import io.mapsmessaging.devices.gpio.pin.BaseDigitalOutput;
import io.mapsmessaging.devices.i2c.I2CBusManager;
import io.mapsmessaging.devices.i2c.I2CDeviceScheduler;
import io.mapsmessaging.devices.i2c.devices.gpio.mcp23017.Mcp23017Controller;
import io.mapsmessaging.devices.i2c.devices.gpio.mcp23017.Mcp23017Device;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class GpioExtension {


  public static void main(String[] args) throws IOException, InterruptedException {
    I2CBusManager[] i2cBusManagers = DeviceBusManager.getInstance().getI2cBusManager();
    int bus = 1;
    if (args.length > 0) {
      bus = Integer.parseInt(args[0]);
    }
    I2CDeviceScheduler deviceController = (I2CDeviceScheduler) i2cBusManagers[bus].configureDevice(0x27, "MCP23017");
    if (deviceController.getDeviceController() instanceof Mcp23017Controller) {

      Pi4JPinManagement pi4JPinManagement = DeviceBusManager.getInstance().getPinManagement();
      Map<String, String> config = new LinkedHashMap<>();
      config.put("id", "piInterrupt0");
      config.put("name", "TestPiIn0");
      config.put("pin", "17");
      BaseDigitalInput piInterrupt0 = pi4JPinManagement.allocateInPin(config);
      Gpio gpio = (Mcp23017Device) deviceController.getDeviceController().getDevice();
      GpioExtensionPinManagement pinManagement = new GpioExtensionPinManagement(gpio, piInterrupt0);
      BaseDigitalOutput[] outputs = new BaseDigitalOutput[gpio.getPins()/2];
      BaseDigitalInput[] inputs = new BaseDigitalInput[gpio.getPins()/2];
      for (int x = 0; x < gpio.getPins()/2; x++) {
        config = new LinkedHashMap<>();
        config.put("id", "ID:" + x);
        config.put("name",  "Out-Name:" + x);
        config.put("pin", ""+x);
        outputs[x] = pinManagement.allocateOutPin(config);
        outputs[x].setLow();
      }
      for(int x=0;x<gpio.getPins()/2;x++){
        config = new LinkedHashMap<>();
        config.put("id", "ID:" + x);
        config.put("name",  "Out-Name:" + x);
        config.put("pin", ""+x+ gpio.getPins()/2);
        inputs[x] = pinManagement.allocateInPin(config);
        BaseDigitalInput input = inputs[x];
        inputs[x].addListener(digitalStateChangeEvent -> System.err.println("Received state change for "+digitalStateChangeEvent.state()+" "+input));
      }
      long time = System.currentTimeMillis() + 60000;
      while (time > System.currentTimeMillis()) {
        for (BaseDigitalOutput out : outputs) {
          out.setHigh();
        }
        Thread.sleep(1000);
        for (BaseDigitalOutput out : outputs) {
          out.setLow();
        }
        Thread.sleep(3000);
      }
    }
  }
}
