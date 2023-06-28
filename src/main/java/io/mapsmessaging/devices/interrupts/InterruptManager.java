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

package io.mapsmessaging.devices.interrupts;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalState;

import java.util.Properties;

public class InterruptManager {

  private final DigitalInput digitalInput;

  public InterruptManager(Context pi4j, String id, String name, int interruptPin, PULL direction, InterruptHandler handler) {
    Properties properties = new Properties();
    properties.put("id", id);
    properties.put("address", interruptPin);
    if (direction.equals(PULL.DOWN)) {
      properties.put("pull", "DOWN");
    } else {
      properties.put("pull", "UP");
    }
    properties.put("name", name);

    var config = DigitalInput.newConfigBuilder(pi4j)
        .load(properties)
        .build();
    digitalInput = pi4j.din().create(config);
    digitalInput.addListener(digitalStateChangeEvent -> {
      if (digitalStateChangeEvent.state().equals(DigitalState.HIGH)) {
        handler.high();
      } else if (digitalStateChangeEvent.state().equals(DigitalState.LOW)) {
        handler.low();
      }
    });
  }

  public enum PULL {
    DOWN,
    UP
  }

}
