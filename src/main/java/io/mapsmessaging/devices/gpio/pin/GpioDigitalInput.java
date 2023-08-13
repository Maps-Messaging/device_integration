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

package io.mapsmessaging.devices.gpio.pin;

import com.pi4j.io.gpio.digital.DigitalState;
import io.mapsmessaging.devices.deviceinterfaces.Gpio;

import java.io.IOException;

public class GpioDigitalInput extends BaseDigitalInput {

  private final Gpio gpio;

  public GpioDigitalInput(String id, String name, Gpio gpio, int pin, boolean pullUp) throws IOException {
    super(pin, id, name);
    this.gpio = gpio;
    gpio.setInput(pin);
    if (pullUp) {
      gpio.enablePullUp(pin);
    } else {
      gpio.disablePullUp(pin);
    }
  }

  @Override
  public DigitalState getState() throws IOException {
    if (gpio.isSet(pin)) {
      return DigitalState.HIGH;
    }
    return DigitalState.LOW;
  }

  @Override
  public String toString() {
    return id + " " + name;
  }

}

