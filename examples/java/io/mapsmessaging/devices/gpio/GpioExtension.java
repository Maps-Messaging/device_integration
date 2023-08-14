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

import com.pi4j.io.gpio.digital.DigitalState;
import io.mapsmessaging.devices.DeviceBusManager;
import io.mapsmessaging.devices.deviceinterfaces.Gpio;
import io.mapsmessaging.devices.gpio.pin.BaseDigitalInput;
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

      Pi4JPinManagement pi4JPinManagement = DeviceBusManager.getInstance().getPinManagement();
      BaseDigitalInput piInterrupt0 = pi4JPinManagement.allocateInPin("piInterrupt0", "TestPiIn0", 17, true);
      BaseDigitalInput piInterrupt1 = pi4JPinManagement.allocateInPin("piInterrupt1", "TestPiIn2", 27, true);

      BaseDigitalInput[] piTest = new BaseDigitalInput[2];

      piTest[0] = pi4JPinManagement.allocateInPin("piIn0", "TestPiIn23", 23, false);
      piTest[1] = pi4JPinManagement.allocateInPin("piIn1", "TestPiIn24", 24, false);


      Gpio gpio = (Mcp23017Device) deviceController.getDeviceController().getDevice();
      GpioExtensionPinManagement pinManagement = new GpioExtensionPinManagement(gpio);
      BaseDigitalOutput[] outputs = new BaseDigitalOutput[gpio.getPins()/2];
      BaseDigitalInput[] inputs = new BaseDigitalInput[gpio.getPins()/2];
      for (int x = 0; x < gpio.getPins()/2; x++) {
        outputs[x] = pinManagement.allocateOutPin("ID:" + x, "Out-Name:" + x, x, false);
        outputs[x].setLow();
      }
      for(int x=0;x<gpio.getPins()/2;x++){
        inputs[x] = pinManagement.allocateInPin("ID:" + x, "In-Name:" + x, x+ gpio.getPins()/2, false);
      }
      while (true) {
        for (BaseDigitalOutput out : outputs) {
          out.setHigh();
        }
        Thread.sleep(1000);
        for (BaseDigitalInput in : inputs) {
          if(!in.getState().equals(DigitalState.HIGH)){
            System.err.println("This isn't right!! !High "+in);
          }
          Thread.sleep(100);
        }
        for(BaseDigitalInput in:piTest){
          if(!in.getState().equals(DigitalState.HIGH)){
            System.err.println("Pi Pin not high "+in);
          }
        }
        Thread.sleep(2000);
        for (BaseDigitalOutput out : outputs) {
          out.setLow();
        }
        Thread.sleep(1000);
        for (BaseDigitalInput in : inputs) {
          if(!in.getState().equals(DigitalState.LOW)){
            System.err.println("This isn't right!! !Low"+in);
          }
          Thread.sleep(100);
        }
        for(BaseDigitalInput in:piTest){
          if(!in.getState().equals(DigitalState.LOW)){
            System.err.println("Pi Pin not low "+in);
          }
        }

        Thread.sleep(2000);
      }
    }
  }
}
