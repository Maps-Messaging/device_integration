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
public enum Odr {
  HERTZ_1(0b0000),
  HERTZ_1_95(0b0001),
  HERTZ_3_9(0b0010),
  HERTZ_7_81(0b0011),
  HERTZ_15_63(0b0100),
  HERTZ_31_25(0b0101),
  HERTZ_62_5(0b0110),
  HERTZ_125(0b0111),
  HERTZ_250(0b1000),
  HERTZ_500(0b1001),
  HERTZ_1000(0b1010);

  private final byte mask;

  Odr(int mask) {
    this.mask = (byte) mask;
  }
}
