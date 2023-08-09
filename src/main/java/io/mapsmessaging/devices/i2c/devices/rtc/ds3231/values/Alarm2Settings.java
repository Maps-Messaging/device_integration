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
