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

package io.mapsmessaging.devices.i2c.devices.sensors.lps25.values;

import lombok.Getter;

@Getter
public enum FiFoMode {
  BYPASS(0b000),
  FIFO(0b001),
  STREAM(0b010),
  STREAM_TO_FIFO(0b011),
  BYPASS_TO_STREAM(0b100),
  RESERVED(0b101),
  FIFO_MEAN(0b110),
  BYPASS_TO_FIFO(0b111);

  private final int mask;

  FiFoMode(int mask) {
    this.mask = mask;
  }
}
