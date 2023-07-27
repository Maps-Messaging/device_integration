package io.mapsmessaging.devices.i2c.devices.rtc.ds3231.values;

import lombok.Getter;

public enum Alarm1Settings {
  ONCE_PER_SECOND(0b1111, false),
  SECONDS(0b1110, false),
  MINUTES_SECONDS(0b1100, false),
  HOURS_MINUTES_SECONDS(0b1000, false),
  DAY_HOUR_MINUTE_SECOND(0b0000, true),
  DATE_HOUR_MINUTE_SECOND(0b0000, false);

  @Getter
  private final int mask;
  @Getter
  private final boolean day;

  Alarm1Settings(int mask, boolean day){
    this.mask = mask;
    this.day = day;
  }

  public static Alarm1Settings find(int mask, boolean day){
    if(mask == 0)day = false;
    for(Alarm1Settings alarm1Settings:values()){
      if(alarm1Settings.mask == mask && alarm1Settings.isDay() == day){
        return alarm1Settings;
      }
    }
    return DATE_HOUR_MINUTE_SECOND;
  }
}
