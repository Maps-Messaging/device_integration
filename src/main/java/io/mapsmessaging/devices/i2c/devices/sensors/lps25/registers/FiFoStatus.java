package io.mapsmessaging.devices.i2c.devices.sensors.lps25.registers;

import lombok.Getter;

public class FiFoStatus {

  @Getter
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
