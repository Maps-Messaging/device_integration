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

package io.mapsmessaging.devices.direct;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalOutput;

import java.util.Properties;

public class PinManagement {

  private final Context pi4j;

  public PinManagement(Context pi4J) {
    this.pi4j = pi4J;
  }

  public DigitalOutput allocateGPIOPin(String id, String name, int pin, String pullDirection) {
    Properties properties = new Properties();
    properties.put("id", id);
    properties.put("address", pin);
    properties.put("pull", pullDirection);
    properties.put("name", name);

    var config = DigitalOutput.newConfigBuilder(pi4j)
        .load(properties)
        .build();
    return pi4j.dout().create(config);
  }
}
