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

package io.mapsmessaging.devices.i2c.devices.rtc.ds3231.values;

import lombok.Getter;

@Getter
public enum Alarm2Settings {
  ONCE_PER_MINUTE(0b111, false),
  MINUTES(0b110, false),
  HOURS_MINUTES(0b100, false),
  DAY_HOUR_MINUTE(0b000, true),
  DATE_HOUR_MINUTE(0b000, false);

  private final int mask;
  @Getter
  private final boolean day;

  Alarm2Settings(int mask, boolean day) {
    this.mask = mask;
    this.day = day;
  }

  public static Alarm2Settings find(int mask, boolean day) {
    if (mask == 0) day = false;
    for (Alarm2Settings alarm2Settings : values()) {
      if (alarm2Settings.mask == mask && alarm2Settings.isDay() == day) {
        return alarm2Settings;
      }
    }
    return DATE_HOUR_MINUTE;
  }
}
