package io.mapsmessaging.devices.i2c.devices.sensors.ina219;

public enum OperatingMode {
  POWERDOWN(0x0000),
  SVOLT_TRIGGERED(0x0001),
  BVOLT_TRIGGERED(0x0002),
  SANDBVOLT_TRIGGERED(0x0003),
  ADCOFF(0x0004),
  SVOLT_CONTINUOUS(0x0005),
  BVOLT_CONTINUOUS(0x0006),
  SANDBVOLT_CONTINUOUS(0x0007);

  private final int value;

  OperatingMode(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}