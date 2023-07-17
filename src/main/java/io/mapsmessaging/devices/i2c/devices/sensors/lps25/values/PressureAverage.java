package io.mapsmessaging.devices.i2c.devices.sensors.lps25.values;

public enum PressureAverage {
  AVERAGE_8(0b00),
  AVERAGE_32(0b01),
  AVERAGE_128(0b10),
  AVERAGE_512(0b11);

  private final int mask;

  PressureAverage(int mask) {
    this.mask = mask;
  }

  public int getMask() {
    return mask;
  }
}
