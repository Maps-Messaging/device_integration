package io.mapsmessaging.devices.i2c.devices.sensors.bh1750;

import lombok.Getter;

public enum ResolutionMode {

  H_RESOLUTION_MODE(0b00000000, 1f),
  H_RESOLUTION_MODE_2(0B00000001, 2f),
  L_RESOLUTION_MODE(0b00000011, 1f);

  @Getter
  private final int mask;

  @Getter
  private final float adjustment;

  ResolutionMode(int mask, float adjustment) {
    this.mask = mask;
    this.adjustment = adjustment;
  }
}
