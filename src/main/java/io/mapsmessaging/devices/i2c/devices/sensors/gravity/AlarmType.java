package io.mapsmessaging.devices.i2c.devices.sensors.gravity;

public enum AlarmType {
  LOW_THRESHOLD((byte) 0x00),
  HIGH_THRESHOLD((byte) 0x01);

  private final byte value;

  AlarmType(byte value) {
    this.value = value;
  }

  public byte getValue() {
    return value;
  }
}
