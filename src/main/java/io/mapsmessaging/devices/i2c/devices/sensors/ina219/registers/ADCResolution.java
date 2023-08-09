package io.mapsmessaging.devices.i2c.devices.sensors.ina219.registers;

import lombok.Getter;

@Getter
public enum ADCResolution {
  RES_9BIT(0x0080),  // 9-bit bus res = 0..511
  RES_10BIT(0x0100),  // 10-bit bus res = 0..1023
  RES_11BIT(0x0200),  // 11-bit bus res = 0..2047
  RES_12BIT(0x0400);  // 12-bit bus res = 0..4097

  private final int value;

  ADCResolution(int value) {
    this.value = value;
  }

}
