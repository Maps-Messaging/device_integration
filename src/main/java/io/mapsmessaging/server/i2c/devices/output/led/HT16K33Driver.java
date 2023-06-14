package io.mapsmessaging.server.i2c.devices.output.led;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.server.i2c.I2CDevice;
import lombok.Getter;

public abstract class HT16K33Driver extends I2CDevice {

  @Getter
  private byte brightness;
  @Getter
  private boolean isOn;
  @Getter
  private boolean blinkOn;
  @Getter
  private boolean fastBlink;
  @Getter
  private String current;

  protected HT16K33Driver(I2C device) {
    super(device);
  }

  public void close() {
    turnOff();
  }

  public abstract byte[] encode(String val);

  public void turnOn() {
    write((byte) 0x21); // Turn on
    write((byte) 0x81); // Turn on display
    byte[] empty = new byte[8];
    write(empty);
    isOn = true;
  }

  public void turnOff() {
    write((byte) 0x20); // Turn off
    isOn = false;
  }

  public void enableBlink(boolean enable, boolean fast) {
    blinkOn = enable;
    fastBlink = fast;
    byte val = (byte) 0x81;
    if (enable) {
      if (fast)
        val = (byte) (val | 0x6);
      else
        val = (byte) (val | 0x2);
    }
    write(val);
  }

  public void setBrightness(byte brightness) {
    this.brightness = (byte) (brightness & 0xf);
    byte val = (byte) (0xE0 | (brightness & 0xf));
    write(val); //Brightness
  }

  public void write(String val) {
    current = val;
    write(0, encode(val));
  }

}
