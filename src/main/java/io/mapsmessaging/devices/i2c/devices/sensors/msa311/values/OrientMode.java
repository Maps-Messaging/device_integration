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

public enum OrientMode {
  SYMMETRICAL(0b00),
  HIGH_ASYMMETRICAL(0b01),
  LOW_ASYMMETRICAL(0b10),
  SYMMETRICAL2(0b11);

  private final int value;

  OrientMode(int value) {
    this.value = value;
  }

  public static OrientMode fromValue(int value) {
    for (OrientMode mode : OrientMode.values()) {
      if (mode.value == value) {
        return mode;
      }
    }
    throw new IllegalArgumentException("Invalid OrientMode value: " + value);
  }

  public int getValue() {
    return value;
  }
}