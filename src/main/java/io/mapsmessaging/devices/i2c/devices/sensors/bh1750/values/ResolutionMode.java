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

package io.mapsmessaging.devices.i2c.devices.sensors.bh1750.values;

import lombok.Getter;

@Getter
public enum ResolutionMode {

  H_RESOLUTION_MODE(0b00000000, 1f, 120),
  H_RESOLUTION_MODE_2(0B00000001, 2f, 16),
  L_RESOLUTION_MODE(0b00000011, 1f, 120);

  private final int mask;

  @Getter
  private final float adjustment;

  @Getter
  private final int delay;

  ResolutionMode(int mask, float adjustment, int delay) {
    this.mask = mask;
    this.adjustment = adjustment;
    this.delay = delay;
  }
}
