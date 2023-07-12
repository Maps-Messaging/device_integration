package io.mapsmessaging.devices.i2c.devices.sensors.lps25.values;

import lombok.Getter;

public enum DataReadyInterrupt {
  ORDER_OF_PRIORITY(0),
  HIGH(1),
  LOW(2),
  LOW_OR_HIGH(3);

  @Getter
  private final int mask;

  DataReadyInterrupt(int mask) {
    this.mask = mask;
  }
}