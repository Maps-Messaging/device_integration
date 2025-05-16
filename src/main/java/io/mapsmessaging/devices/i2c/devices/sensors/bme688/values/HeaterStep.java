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

package io.mapsmessaging.devices.i2c.devices.sensors.bme688.values;

public enum HeaterStep {

  NONE(0),
  STEP_1(1),
  STEP_2(2),
  STEP_3(3),
  STEP_4(4),
  STEP_5(5),
  STEP_6(6),
  STEP_7(7),
  STEP_8(9),
  STEP_9(9);


  private final int value;

  HeaterStep(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
