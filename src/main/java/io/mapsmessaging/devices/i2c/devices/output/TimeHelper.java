package io.mapsmessaging.devices.i2c.devices.output;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TimeHelper {

  public static String getTime(boolean useSpace, boolean addSeconds) {
    // Define format patterns based on the parameters
    String separator = useSpace ? " " : ":";
    String formatPattern;

    if (addSeconds) {
      formatPattern = "HH" + separator + "mm" + separator + "ss";
    } else {
      formatPattern = "HH" + separator + "mm";
    }

    // Get the current time
    LocalTime currentTime = LocalTime.now();

    // Format and return the time string
    return currentTime.format(DateTimeFormatter.ofPattern(formatPattern));
  }

  private TimeHelper() {
  }
}
