package io.mapsmessaging.devices.i2c.devices.sensors.lps35.values;

import lombok.Getter;

public enum DataRate {
  RATE_ONE_SHOT(0b0000000),
  RATE_1_HZ(0b0010000),
  RATE_10_HZ(0b0100000),
  RATE_25_HZ(0b0110000),
  RATE_50_HZ(0b1000000),
  RATE_75_HZ(0b1010000);

  @Getter
  private final int mask;

  DataRate(int mask) {
    this.mask = mask;
  }
}
