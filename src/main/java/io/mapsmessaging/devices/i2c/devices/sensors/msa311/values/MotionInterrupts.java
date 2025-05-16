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
public enum MotionInterrupts {
  ORIENTATION((byte) 0b01000000),
  SINGLE_TAP((byte) 0b00100000),
  DOUBLE_TAP((byte) 0b00010000),
  ACTIVE((byte) 0b00000100),
  FREEFALL((byte) 0b00000001);

  private final byte mask;

  MotionInterrupts(byte mask) {
    this.mask = mask;
  }
}
