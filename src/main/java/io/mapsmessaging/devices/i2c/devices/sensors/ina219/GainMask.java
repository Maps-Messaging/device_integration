package io.mapsmessaging.devices.i2c.devices.sensors.ina219;

public enum GainMask {
  GAIN_1_40MV(0x0000),  // Gain 1, 40mV Range
  GAIN_2_80MV(0x0800),  // Gain 2, 80mV Range
  GAIN_4_160MV(0x1000),  // Gain 4, 160mV Range
  GAIN_8_320MV(0x1800);  // Gain 8, 320mV Range

  private final int value;

  GainMask(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
