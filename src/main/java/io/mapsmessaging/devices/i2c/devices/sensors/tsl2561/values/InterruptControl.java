package io.mapsmessaging.devices.i2c.devices.sensors.tsl2561.values;

import lombok.Getter;

@Getter
public enum InterruptControl {

  DISABLED(0b00),
  LEVEL(0b01),
  SMB_ALERT(0b10),
  TEST(0b11);

  private final byte mask;

  InterruptControl(int mask) {
    this.mask = (byte) mask;
  }
}
