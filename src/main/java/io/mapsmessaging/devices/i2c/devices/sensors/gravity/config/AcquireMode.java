package io.mapsmessaging.devices.i2c.devices.sensors.gravity.config;

import lombok.Getter;

public enum AcquireMode {
  INITIATIVE((byte) 0x03),
  PASSIVITY((byte) 0x04);

  @Getter
  private final byte value;

  AcquireMode(byte value) {
    this.value = value;
  }

}
