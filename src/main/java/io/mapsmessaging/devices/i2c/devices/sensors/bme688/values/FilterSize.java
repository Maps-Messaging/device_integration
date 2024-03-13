package io.mapsmessaging.devices.i2c.devices.sensors.bme688.values;


public enum FilterSize {
  OFF(0),
  SIZE_1(1),
  SIZE_3(2),
  SIZE_7(3),
  SIZE_15(4),
  SIZE_31(5),
  SIZE_63(6),
  SIZE_127(7);

  private final int value;

  FilterSize(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}