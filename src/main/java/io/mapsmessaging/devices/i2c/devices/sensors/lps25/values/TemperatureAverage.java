package io.mapsmessaging.devices.i2c.devices.sensors.lps25.values;

public enum TemperatureAverage {
  AVERAGE_8(0b00),
  AVERAGE_16(0b01),
  AVERAGE_32(0b10),
  AVERAGE_64(0b11);

  private final int mask;

  TemperatureAverage(int mask) {
    this.mask = mask;
  }

  public int getMask() {
    return mask;
  }
}
