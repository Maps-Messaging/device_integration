package io.mapsmessaging.server.i2c.devices.output.led;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.server.i2c.I2CDevice;

public class QuadAlphaNumeric extends I2CDevice {


  /*
  send 0x21 - start the oscillator
  send 0xEF - set brightness to max (not really required, but a bit of a sanity check).
  send 0x81 - turn blink off and display on

  To unravel this
  0x21 = 0x20 + 0x01 // 0x20 = System setup command. 0x01 = S bit (1 = oscillator on, 0 = oscillator off)
  0xEF = 0xE0 + 0x0F // 0xE0 = brightness command. 0x0F = 16/16 duty cycle. (valid values are 0x00 - 0x0F for 1/16 to 16/16 duty cycle)
  0x81 = 0x80 + 0x01 // 0x80 = blink command. 0x01 = display on (add 0x06 for .5hz blink, 0x04 for 1hz blink, 0x02 for 1hz blink)

   */

  private final byte[] buf = new byte[8];

  public QuadAlphaNumeric(I2C device) {
    super(device);
  }

  public static void encode(String val, byte[] buf) {
    for (int x = 0; x < buf.length; x++)
      buf[0] = 0;
    int len = val.length();
    int bufIdx = 0;
    for (int x = 0; x < len; x++) {
      short map = QuadAlphaHelper.ALPHA_NUMERIC_MAPPING[val.charAt(x)];
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
  }

  public void close() {
    turnOff();
  }

  public void turnOn() {
    device.write((byte) 0x21); // Turn on
    device.write((byte) 0x81); // Turn on display
  }

  public void turnOff() {
    device.write((byte) 0x20); // Turn off
  }

  public void enableBlink(boolean enable, boolean fast) {
    byte val = (byte) 0x81;
    if (enable) {
      if (fast)
        val = (byte) (val | 0x6);
      else
        val = (byte) (val | 0x2);
    }
    device.write(val);
  }

  public void setBrightness(byte brightness) {
    byte val = (byte) (0xE0 | (brightness & 0xf));
    device.write(val); //Brightness
  }

  public void write(byte[] raw) {
    device.write(raw, 0, raw.length);
  }

  public void write(String val) {
    encode(val, buf);
    device.write(buf, 0, buf.length);
  }
}