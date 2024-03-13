package io.mapsmessaging.devices.i2c.devices.sensors.bme688.values;

public enum HeaterControl {
  ENABLE(0),
  DISABLE(1);

  private final int value;

  HeaterControl(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}