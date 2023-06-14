package io.mapsmessaging.server.i2c.devices.output.led;

public class Quad7SegmentHelper {
  public static final byte[] NUMERIC_MAPPING = {
      (byte)0b00111111, // 0
      (byte)0b00000110, // 1
      (byte)0b01011011, // 2
      (byte)0b01001111, // 3
      (byte)0b01100110, // 4
      (byte)0b01101101, // 5
      (byte)0b01111101, // 6
      (byte)0b00000111, // 7
      (byte)0b01111111, // 8
      (byte)0b01101111, // 9
  };
}
