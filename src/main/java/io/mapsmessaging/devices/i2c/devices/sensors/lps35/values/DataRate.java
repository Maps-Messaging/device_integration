/*
 *
 *  Copyright [ 2020 - 2024 ] [Matthew Buckton]
 *  Copyright [ 2024 - 2025.  ] [Maps Messaging B.V.]
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package io.mapsmessaging.devices.i2c.devices.sensors.lps35.values;

import lombok.Getter;

@Getter
public enum DataRate {
  RATE_ONE_SHOT(0b0000000),
  RATE_1_HZ(0b0010000),
  RATE_10_HZ(0b0100000),
  RATE_25_HZ(0b0110000),
  RATE_50_HZ(0b1000000),
  RATE_75_HZ(0b1010000);

  private final int mask;

  DataRate(int mask) {
    this.mask = mask;
  }
}
