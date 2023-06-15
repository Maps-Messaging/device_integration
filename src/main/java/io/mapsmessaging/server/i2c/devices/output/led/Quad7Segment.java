package io.mapsmessaging.server.i2c.devices.output.led;

import com.pi4j.io.i2c.I2C;

public class Quad7Segment extends HT16K33Driver {

  private final byte[] buf = new byte[10];

  public Quad7Segment(I2C device) {
    super(device);
  }

  public byte[] encode(String val) {
    for (int x = 0; x < buf.length; x++) buf[0] = 0;
    int len = val.length();
    int bufIdx = 0;
    for (int x = 0; x < len; x++) {
      char c = val.charAt(x);
      byte map = 0;
      if (c != ' ') {
        if(!Character.isDigit(c)){
          map = -1;
        }
        else {
          int index = (c - 0x30);
          map = Constants.NUMERIC_MAPPING[index];
        }
      }
      buf[bufIdx * 2] = (byte) (map & 0xff);
      buf[bufIdx * 2 + 1] = (byte) (0);
      if (x + 1 < len && val.charAt(x + 1) == '.') {
        buf[bufIdx * 2] = (byte) (buf[bufIdx * 2 + 1] | 0b10000000);
        x++; // Set the . and skip to the next char
      }
      bufIdx++;
      if (bufIdx > 4) break;
    }
    return buf;
  }

}