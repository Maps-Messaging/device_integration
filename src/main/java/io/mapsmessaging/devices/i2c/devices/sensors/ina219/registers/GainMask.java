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

package io.mapsmessaging.devices.i2c.devices.sensors.ina219.registers;

import lombok.Getter;

@Getter
public enum GainMask {
  GAIN_1_40MV(0x0000),  // Gain 1, 40mV Range
  GAIN_2_80MV(0x0800),  // Gain 2, 80mV Range
  GAIN_4_160MV(0x1000),  // Gain 4, 160mV Range
  GAIN_8_320MV(0x1800);  // Gain 8, 320mV Range

  private final int value;

  GainMask(int value) {
    this.value = value;
  }

}
