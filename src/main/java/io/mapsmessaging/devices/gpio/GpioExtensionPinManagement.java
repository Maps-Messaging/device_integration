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

public class GpioExtensionPinManagement implements PinManagement {

  private final Gpio gpio;

  public GpioExtensionPinManagement(Gpio gpio) {
    this.gpio = gpio;
  }

  public BaseDigitalOutput allocateOutPin(String id, String name, int pin, boolean pullUp) throws IOException {
    return new GpioDigitalOutput(id, name, gpio, pin, pullUp);
  }

  public BaseDigitalInput allocateInPin(String id, String name, int pin, boolean pullUp) throws IOException {
    return new GpioDigitalInput(id, name, gpio, pin, pullUp);
  }

}
