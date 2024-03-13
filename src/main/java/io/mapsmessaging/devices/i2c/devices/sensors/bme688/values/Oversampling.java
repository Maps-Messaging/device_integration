package io.mapsmessaging.devices.i2c.devices.sensors.bme688.values;

public enum Oversampling {

  NONE(0),
  X1(1),
  X2(2),
  X4(3),
  X8(4),
  X16(5);

  private final int value;

  Oversampling(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
