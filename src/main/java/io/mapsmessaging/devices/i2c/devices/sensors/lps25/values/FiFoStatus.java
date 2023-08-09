package io.mapsmessaging.devices.i2c.devices.sensors.lps25.values;

import lombok.Getter;

@Getter
public class FiFoStatus {

  private final boolean hitThreshold;

  @Getter
  private final boolean isOverwritten;

  @Getter
  private final int size;

  public FiFoStatus(boolean hit, boolean overwritten, int size) {
    hitThreshold = hit;
    isOverwritten = overwritten;
    this.size = size;
  }
}
