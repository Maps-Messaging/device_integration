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

import io.mapsmessaging.devices.deviceinterfaces.Gpio;
import io.mapsmessaging.devices.gpio.pin.BaseDigitalInput;
import io.mapsmessaging.devices.gpio.pin.BaseDigitalOutput;
import io.mapsmessaging.devices.gpio.pin.GpioDigitalInput;
import io.mapsmessaging.devices.gpio.pin.GpioDigitalOutput;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GpioExtensionPinManagement extends PinManagement implements InterruptHandler {

  private final Gpio gpio;
  private final Map<Integer, BaseDigitalInput> interruptMap;
  private final InterruptExecutor interruptExecutor;

  public GpioExtensionPinManagement(Gpio gpio) {
    this.gpio = gpio;
    interruptMap = new ConcurrentHashMap<>();
    interruptExecutor = new ThreadInterruptExecutor(this);
  }

  public GpioExtensionPinManagement(Gpio gpio, BaseDigitalInput interruptInput) {
    this.gpio = gpio;
    interruptMap = new ConcurrentHashMap<>();
    interruptInput.addListener(digitalStateChangeEvent -> interruptFired());
    interruptExecutor = new ThreadInterruptExecutor(this);
  }

  public void close() throws IOException {
    interruptExecutor.close();
  }

  public BaseDigitalOutput allocateOutPin(String id, String name, int pin, boolean pullUp) throws IOException {
    return new GpioDigitalOutput(id, name, gpio, pin, pullUp);
  }

  public BaseDigitalInput allocateInPin(String id, String name, int pin, boolean pullUp) throws IOException {
    BaseDigitalInput input = new GpioDigitalInput(id, name, gpio, pin, pullUp);
    interruptMap.put(pin, input);
    return input;
  }

  public void interruptFired()  {
    try {
      int[] list = gpio.getInterrupted();
      for(int port:list){
        BaseDigitalInput input = interruptMap.get(port);
        if(input != null){
          input.stateChange();
        }
      }
    } catch (IOException e) {
      //
    }
  }
}
