package io.mapsmessaging.devices.i2c.devices.output;

import java.time.LocalTime;

public class TimeHelper {

  public static String getTime(boolean hasColon, boolean addSeconds){
    StringBuilder val = new StringBuilder();
    LocalTime dateTime = LocalTime.now();
    int hour = dateTime.getHour();
    int min = dateTime.getMinute();
    int sec = dateTime.getSecond();
    if (hour < 10) {
      val.append("0");
    }
    val.append(hour);

    if (hasColon) {
      val.append(" ");
    } else {
      val.append(":");
    }
    if (min < 10) {
      val.append("0");
    }
    val.append(min);
    if(addSeconds){
      if (hasColon) {
        val.append(" ");
      } else {
        val.append(":");
      }
      if (sec < 10) {
        val.append("0");
      }
      val.append(sec);
    }
    return val.toString();
  }
}
