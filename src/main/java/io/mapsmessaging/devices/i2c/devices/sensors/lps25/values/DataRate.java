package io.mapsmessaging.devices.i2c.devices.sensors.lps25.values;

import lombok.Getter;

@Getter
public enum DataRate {
  RATE_ONE_SHOT(0b000),
  RATE_1_HZ(0b001),
  RATE_7_HZ(0b010),
  RATE_12_5_HZ(0b011),
  RATE_25_HZ(0b100);

  private final int mask;

  DataRate(int mask) {
    this.mask = mask;
  }
}
