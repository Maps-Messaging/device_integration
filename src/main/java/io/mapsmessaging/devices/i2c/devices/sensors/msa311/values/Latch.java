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

package io.mapsmessaging.devices.i2c.devices.sensors.msa311.values;

import lombok.Getter;

@Getter
public enum Latch {
  NON_LATCHED(0b0000),
  TEMP_250MS(0b0001),
  TEMP_500MS(0b0010),
  TEMP_1S(0b0011),
  TEMP_2S(0b0100),
  TEMP_4S(0b0101),
  TEMP_8S(0b0110),
  LATCHED(0b0111),
  NON_LATCHED_2(0b1000),
  TEMP_1MS(0b1001),
  TEMP_2MS(0b1010),
  TEMP_25MS(0b1100),
  TEMP_50MS(0b1101),
  TEMP_100MS(0b1110);

  private final byte mask;

  Latch(int mask) {
    this.mask = (byte) mask;
  }
}
