package io.mapsmessaging.devices.i2c.devices.sensors.msa311.values;

import lombok.Getter;

public enum LowPowerBandwidth {

  HERTZ_1_95(0b0000, 0b0010),
  HERTZ_3_9(0b0011),
  HERTZ_7_81(0b0100),
  HERTZ_15_63(0b0101),
  HERTZ_31_25(0b0110),
  HERTZ_62_5(0b0111),
  HERTZ_125(0b1000),
  HERTZ_250(0b1001),
  HERTZ_500(0b1010, 0b1111);

  @Getter
  private final byte start;
  @Getter
  private final byte end;

  LowPowerBandwidth(int start){
    this(start, start);
  }


  LowPowerBandwidth(int start, int end){
    this.start = (byte) start;
    this.end = (byte) end;
  }

}
