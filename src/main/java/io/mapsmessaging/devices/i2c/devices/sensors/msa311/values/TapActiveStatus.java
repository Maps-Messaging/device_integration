package io.mapsmessaging.devices.i2c.devices.sensors.msa311.values;

import lombok.Getter;

public enum TapActiveStatus {

  SIGN((byte) 0b10000000),
  FIRST_X((byte) 0b01000000),
  FIRST_Y((byte) 0b00100000),
  FIRST_Z((byte) 0b00010000),
  ACTIVE_SIGN((byte) 0b00001000),
  ACTIVE_FIRST_X((byte) 0b00000100),
  ACTIVE_FIRST_Y((byte) 0b00000010),
  ACTIVE_FIRST_Z((byte) 0b00000001);

  @Getter
  private final byte mask;

  TapActiveStatus(byte mask){
    this.mask = mask;
  }
}
