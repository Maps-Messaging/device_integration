package io.mapsmessaging.devices.i2c.devices.sensors.msa311.values;

import lombok.Getter;

public enum OrientationStatus {
  Z_UP_PORTRAIT_UPRIGHT((byte)0b000),
  Z_UP_PORTRAIT_UPSIDE_DOWN((byte)0b001),
  Z_UP_LANDSCAPE_LEFT((byte)0b010),
  Z_UP_LANDSCAPE_RIGHT((byte)0b011),
  Z_DOWN_PORTRAIT_UPRIGHT((byte)0b100),
  Z_DOWN_PORTRAIT_UPSIDE_DOWN((byte)0b101),
  Z_DOWN_LANDSCAPE_LEFT((byte)0b110),
  Z_DOWN_LANDSCAPE_RIGHT((byte)0b111);

  @Getter
  private final byte mask;

  OrientationStatus(byte mask){
    this.mask = mask;
  }
}
