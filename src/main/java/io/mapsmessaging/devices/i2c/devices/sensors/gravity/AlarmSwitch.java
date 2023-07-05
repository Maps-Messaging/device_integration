package io.mapsmessaging.devices.i2c.devices.sensors.gravity;

public enum AlarmSwitch {
  ON((byte) 0x01),
  OFF((byte) 0x00);

  private final byte value;

  AlarmSwitch(byte value) {
    this.value = value;
  }

  public byte getValue() {
    return value;
  }
}