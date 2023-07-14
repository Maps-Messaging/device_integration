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

public enum TapDuration {
  TAP_QUIET_20MS(20),
  TAP_QUIET_30MS(30),
  TAP_SHOCK_50MS(50),
  TAP_SHOCK_70MS(70),
  TAP_SECOND_SHOCK_50MS(50),
  TAP_SECOND_SHOCK_100MS(100),
  TAP_SECOND_SHOCK_150MS(150),
  TAP_SECOND_SHOCK_200MS(200),
  TAP_SECOND_SHOCK_250MS(250),
  TAP_SECOND_SHOCK_375MS(375),
  TAP_SECOND_SHOCK_500MS(500),
  TAP_SECOND_SHOCK_700MS(700);

  private final int value;

  TapDuration(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

  public static TapDuration fromValue(int value) {
    for (TapDuration duration : TapDuration.values()) {
      if (duration.getValue() == value) {
        return duration;
      }
    }
    throw new IllegalArgumentException("Invalid TapDuration value: " + value);
  }
}