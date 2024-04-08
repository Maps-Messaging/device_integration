package io.mapsmessaging.devices.i2c.devices.sensors.lps25.values;

import lombok.Getter;

@Getter
public enum FiFoSampleSize {

  TWO("2 sample size", 0b00001),
  FOUR("4 sample size", 0b00011),
  EIGHT("8 sample size", 0b00111),
  SIXTEEN("16 sample size", 0b01111),
  THIRTY_TWO("32 sample size", 0b11111);


  private final int mask;
  private final String name;

  FiFoSampleSize(String name, int mask){
    this.mask = mask;
    this.name = name;
  }
}
