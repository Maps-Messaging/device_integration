package io.mapsmessaging.devices.i2c.devices.sensors.lps25.values;

import lombok.Getter;

@Getter
public enum FiFoMode {
  BYPASS(0b000),
  FIFO(0b001),
  STREAM(0b010),
  STREAM_TO_FIFO(0b011),
  BYPASS_TO_STREAM(0b100),
  RESERVED(0b101),
  FIFO_MEAN(0b110),
  BYPASS_TO_FIFO(0b111);

  private final int mask;

  FiFoMode(int mask) {
    this.mask = mask;
  }
}
