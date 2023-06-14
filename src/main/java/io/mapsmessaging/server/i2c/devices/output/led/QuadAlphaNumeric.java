package io.mapsmessaging.server.i2c.devices.output.led;

import com.pi4j.io.i2c.I2C;

public class QuadAlphaNumeric extends HT16K33Driver {

  private final byte[] buf = new byte[8];

  public QuadAlphaNumeric(I2C device) {
    super(device);
  }

  public byte[] encode(String val) {
    for (int x = 0; x < buf.length; x++)
      buf[0] = 0;
    int len = val.length();
    int bufIdx = 0;
    for (int x = 0; x < len; x++) {
      short map = Constants.ALPHA_NUMERIC_MAPPING[val.charAt(x)];
      buf[bufIdx * 2] = (byte) (map & 0xff);
      buf[bufIdx * 2 + 1] = (byte) ((map >> 8) & 0xff);
      if (x + 1 < len && val.charAt(x + 1) == '.') {
        buf[bufIdx * 2 + 1] = (byte) (buf[bufIdx * 2 + 1] | 0x40);
        x++; // Set the . and skip to the next char
      }
      bufIdx++;
      if (bufIdx > 3)
        break;
    }
    return buf;
  }
}