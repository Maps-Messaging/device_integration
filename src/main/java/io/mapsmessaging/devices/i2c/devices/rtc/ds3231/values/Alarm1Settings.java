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

package io.mapsmessaging.devices.i2c.devices.rtc.ds3231.values;

import lombok.Getter;

@Getter
public enum Alarm1Settings {
  ONCE_PER_SECOND(0b1111, false),
  SECONDS(0b1110, false),
  MINUTES_SECONDS(0b1100, false),
  HOURS_MINUTES_SECONDS(0b1000, false),
  DAY_HOUR_MINUTE_SECOND(0b0000, true),
  DATE_HOUR_MINUTE_SECOND(0b0000, false);

  private final int mask;
  @Getter
  private final boolean day;

  Alarm1Settings(int mask, boolean day) {
    this.mask = mask;
    this.day = day;
  }

  public static Alarm1Settings find(int mask, boolean day) {
    if (mask == 0) day = false;
    for (Alarm1Settings alarm1Settings : values()) {
      if (alarm1Settings.mask == mask && alarm1Settings.isDay() == day) {
        return alarm1Settings;
      }
    }
    return DATE_HOUR_MINUTE_SECOND;
  }
}
