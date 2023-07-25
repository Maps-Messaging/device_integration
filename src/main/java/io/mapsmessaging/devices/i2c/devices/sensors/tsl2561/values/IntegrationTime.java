package io.mapsmessaging.devices.i2c.devices.sensors.tsl2561.values;

import lombok.Getter;

public enum IntegrationTime {
  MS_13_7((byte) 0b00, 13.7f, 0.034f),
  MS_101((byte) 0b01, 101, 0.252f),
  MS_402((byte) 0b10, 402, 1),
  MANUAL((byte) 0b11, 0, 1);

  @Getter
  private final byte mask;
  @Getter
  private final float time;
  @Getter
  private final float scale;

  IntegrationTime(byte mask, float integrationTime, float scale) {
    this.mask = mask;
    this.time = integrationTime;
    this.scale = scale;
  }
}