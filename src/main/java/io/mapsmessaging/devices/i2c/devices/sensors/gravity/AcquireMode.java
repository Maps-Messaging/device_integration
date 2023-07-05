package io.mapsmessaging.devices.i2c.devices.sensors.gravity;

public enum AcquireMode {
  INITIATIVE((byte) 0x03),
  PASSIVITY((byte) 0x04);

  private final byte value;

  AcquireMode(byte value) {
    this.value = value;
  }

  public byte getValue() {
    return value;
  }
}
