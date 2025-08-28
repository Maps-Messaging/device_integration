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

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalInputProvider;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalOutputProvider;
import io.mapsmessaging.devices.gpio.pin.BaseDigitalInput;
import io.mapsmessaging.devices.gpio.pin.BaseDigitalOutput;
import io.mapsmessaging.devices.gpio.pin.Pi4JDigitalInput;
import io.mapsmessaging.devices.gpio.pin.Pi4JDigitalOutput;

import java.io.IOException;
import java.util.Map;

public class Pi4JPinManagement extends PinManagement {

  private final Context pi4j;
  private DigitalInputProvider inputProvider;
  private DigitalOutputProvider outputProvider;

  public Pi4JPinManagement(Context pi4J) {
    this.pi4j = pi4J;
    inputProvider = pi4j.getDigitalInputProvider();
    outputProvider = pi4j.getDigitalOutputProvider();
  }

  @Override
  public BaseDigitalOutput allocateOutPin(Map<String, String> properties) {
    var config = DigitalOutput.newConfigBuilder(pi4j)
        .load(properties)
        .build();
    return new Pi4JDigitalOutput(outputProvider.create(config));
  }

  @Override
  public BaseDigitalInput allocateInPin(Map<String, String> properties) {
    var config = DigitalInput.newConfigBuilder(pi4j)
        .load(properties)
        .build();
    return new Pi4JDigitalInput(inputProvider.create(config));
  }

  @Override
  public void close() throws IOException {
    outputProvider.shutdown(pi4j);
    inputProvider.shutdown(pi4j);
  }
}
