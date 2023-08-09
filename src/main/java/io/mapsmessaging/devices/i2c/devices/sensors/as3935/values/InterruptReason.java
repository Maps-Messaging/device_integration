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

package io.mapsmessaging.devices.i2c.devices.sensors.as3935.values;

import lombok.Getter;

@Getter
public enum InterruptReason {
  NONE(0b0000, "None"),
  NT_NH(0b0001, "Noise level too high"),
  INT_D(0b0100, "Disturber detected"),
  INT_L(0b1000, "Lightning interrupt");

  private final int mask;
  @Getter
  private final String description;

  InterruptReason(int mask, String description) {
    this.mask = mask;
    this.description = description;
  }
}
