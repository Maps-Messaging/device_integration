package io.mapsmessaging.devices.i2c.devices.sensors.msa311.values;

import lombok.Getter;

@Getter
public enum Odr {
  HERTZ_1(0b0000),
  HERTZ_1_95(0b0001),
  HERTZ_3_9(0b0010),
  HERTZ_7_81(0b0011),
  HERTZ_15_63(0b0100),
  HERTZ_31_25(0b0101),
  HERTZ_62_5(0b0110),
  HERTZ_125(0b0111),
  HERTZ_250(0b1000),
  HERTZ_500(0b1001),
  HERTZ_1000(0b1010);

  private final byte mask;

  Odr(int mask) {
    this.mask = (byte) mask;
  }
}
