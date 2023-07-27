package io.mapsmessaging.devices.i2c.devices.rtc.ds3231.register;

import io.mapsmessaging.devices.i2c.I2CDevice;

import java.io.IOException;

public class SecondsRegister extends BcdRegister {


  public SecondsRegister(I2CDevice sensor, int address, String name) throws IOException {
    super(sensor,address, name, false );
  }

  public int getSeconds() throws IOException {
    return getValue();
  }

  public void setSeconds(int seconds) throws IOException {
    setValue(seconds);
  }
}
