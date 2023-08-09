package io.mapsmessaging.devices.i2c.devices.sensors.msa311.values;

import lombok.Getter;

@Getter
public enum MotionInterrupts {
  ORIENTATION((byte) 0b01000000),
  SINGLE_TAP((byte) 0b00100000),
  DOUBLE_TAP((byte) 0b00010000),
  ACTIVE((byte) 0b00000100),
  FREEFALL((byte) 0b00000001);

  private final byte mask;

  MotionInterrupts(byte mask) {
    this.mask = mask;
  }
}
