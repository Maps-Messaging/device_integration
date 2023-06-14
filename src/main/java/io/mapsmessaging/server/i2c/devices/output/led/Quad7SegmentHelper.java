package io.mapsmessaging.server.i2c.devices.output.led;

public class Quad7SegmentHelper {
  public static final byte[] NUMERIC_MAPPING = {
      (byte)0b00111111, // 0
      (byte)0b00000110, // 1
      (byte)0b11011011, // 2
      (byte)0b10001111, // 3
      (byte)0b11100110, // 4
      (byte)0b01101001, // 5
      (byte)0b11111101, // 6
      (byte)0b00000111, // 7
      (byte)0b11111111, // 8
      (byte)0b11101111, // 9
  };
}
