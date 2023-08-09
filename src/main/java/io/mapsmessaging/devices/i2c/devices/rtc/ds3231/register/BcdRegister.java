package io.mapsmessaging.devices.i2c.devices.rtc.ds3231.register;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;

import java.io.IOException;

public class BcdRegister extends SingleByteRegister {

  private static final int TOP = 0b10000000;
  private static final int TENS = 0b01110000;
  private static final int UNITSS = 0b00001111;

  private static final int BCD_FLAG = 0b01111111;
  private static final int BCD_TOP_FLAG = 0b11111111;

  private final int bcdMask;

  public BcdRegister(I2CDevice sensor, int address, String name, boolean includeTop) throws IOException {
    super(sensor, address, name);
    if (includeTop) {
      bcdMask = BCD_TOP_FLAG;
    } else {
      bcdMask = BCD_FLAG;
    }
  }

  protected static int bcdToDecimal(int bcdValue) {
    return ((bcdValue & TENS) >> 4) * 10 + (bcdValue & UNITSS);
  }

  protected static byte decimalToBcd(int decimalValue) {
    return (byte) (((decimalValue / 10) << 4) | (decimalValue % 10));
  }

  public boolean isTopSet() {
    return (registerValue & TOP) != 0;
  }

  public void setTop(boolean flag) throws IOException {
    setControlRegister(~TOP, flag ? TOP : 0);
  }

  protected int getValue() throws IOException {
    reload();
    return bcdToDecimal(registerValue & bcdMask);
  }

  protected void setValue(int value) throws IOException {
    setControlRegister(~bcdMask, decimalToBcd(value));
  }
}