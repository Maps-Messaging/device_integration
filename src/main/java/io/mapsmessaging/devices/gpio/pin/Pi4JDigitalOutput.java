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

import com.pi4j.io.gpio.digital.DigitalOutput;

import java.io.IOException;

public class Pi4JDigitalOutput implements BaseDigitalOutput {

  private final DigitalOutput output;

  public Pi4JDigitalOutput(DigitalOutput output) {
    this.output = output;
  }

  @Override
  public void setUp() throws IOException {
    output.high();
  }

  @Override
  public void setDown() throws IOException {
    output.low();
  }
}