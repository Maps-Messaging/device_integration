package io.mapsmessaging.devices.i2c.devices.sensors.bme688.values;

public enum PowerMode {
  SLEEP_MODE(0),
  FORCED_MODE(1),
  PARALLEL_MODE(2);

  private final int value;

  PowerMode(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
