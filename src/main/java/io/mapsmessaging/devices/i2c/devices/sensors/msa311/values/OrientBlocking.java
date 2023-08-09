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

package io.mapsmessaging.devices.i2c.devices.sensors.msa311.values;

import lombok.Getter;

@Getter
public enum OrientBlocking {
  NO_BLOCKING(0b00),
  Z_AXIS_BLOCKING(0b01),
  Z_AXIS_BLOCKING_OR_SLOPE(0b10),
  NO_BLOCKING2(0b11);

  private final int value;

  OrientBlocking(int value) {
    this.value = value;
  }

  public static OrientBlocking fromValue(int value) {
    for (OrientBlocking blocking : OrientBlocking.values()) {
      if (blocking.value == value) {
        return blocking;
      }
    }
    throw new IllegalArgumentException("Invalid OrientBlocking value: " + value);
  }

}