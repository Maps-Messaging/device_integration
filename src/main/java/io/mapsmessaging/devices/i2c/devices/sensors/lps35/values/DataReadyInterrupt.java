package io.mapsmessaging.devices.i2c.devices.sensors.lps35.values;

import lombok.Getter;

@Getter
public enum DataReadyInterrupt {
  ORDER_OF_PRIORITY(0),
  HIGH(1),
  LOW(2),
  LOW_OR_HIGH(3);

  private final int mask;

  DataReadyInterrupt(int mask) {
    this.mask = mask;
  }
}
