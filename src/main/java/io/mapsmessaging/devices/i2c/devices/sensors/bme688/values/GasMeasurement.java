package io.mapsmessaging.devices.i2c.devices.sensors.bme688.values;


public enum GasMeasurement {
  DISABLE(0),
  ENABLE_LOW(1),
  ENABLE_HIGH(2);

  private final int value;

  GasMeasurement(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
