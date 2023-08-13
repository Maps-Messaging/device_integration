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

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalOutput;
import io.mapsmessaging.devices.gpio.pin.BaseDigitalInput;
import io.mapsmessaging.devices.gpio.pin.BaseDigitalOutput;
import io.mapsmessaging.devices.gpio.pin.Pi4JDigitalInput;
import io.mapsmessaging.devices.gpio.pin.Pi4JDigitalOutput;

import java.util.Properties;

public class PiPinManagement implements PinManagement {

  private final Context pi4j;

  public PiPinManagement(Context pi4J) {
    this.pi4j = pi4J;
  }

  @Override
  public BaseDigitalOutput allocateOutPin(String id, String name, int pin, boolean pullUp) {
    Properties properties = new Properties();
    properties.put("id", id);
    properties.put("address", pin);
    if (pullUp) {
      properties.put("pull", "UP");
    }
    properties.put("name", name);

    var config = DigitalOutput.newConfigBuilder(pi4j)
        .load(properties)
        .build();
    return new Pi4JDigitalOutput(pi4j.dout().create(config));
  }

  @Override
  public BaseDigitalInput allocateInPin(String id, String name, int pin, boolean pullUp) {
    Properties properties = new Properties();
    properties.put("id", id);
    properties.put("address", pin);
    if (pullUp) {
      properties.put("pull", "UP");
    }
    properties.put("name", name);

    var config = DigitalInput.newConfigBuilder(pi4j)
        .load(properties)
        .build();
    return new Pi4JDigitalInput(pi4j.din().create(config));
  }

}
