package io.mapsmessaging.devices.i2c.devices.output.led.ht16k33;

import lombok.Getter;

@Getter
public enum SevenSegmentLed {
  TOP(0b00000001),
  TOP_RIGHT(0b00000010),
  BOTTOM_RIGHT(0b00000100),
  BOTTOM(0b00001000),
  BOTTOM_LEFT(0b00010000),
  TOP_LEFT(0b00100000),
  MIDDLE(0b01000000),
  DECIMAL(0b10000000),
  COLON(0b11111111);

  private final int mask;


  SevenSegmentLed(int mask) {
    this.mask = mask;
  }
}
