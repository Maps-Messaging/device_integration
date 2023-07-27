package io.mapsmessaging.devices.i2c.devices.rtc.ds3231.register;

import io.mapsmessaging.devices.i2c.I2CDevice;

import java.io.IOException;

public class MinutesRegister extends BcdRegister {


  public MinutesRegister(I2CDevice sensor, int address, String name) throws IOException {
    super(sensor,address, name, false);
  }

  public int getMinutes() throws IOException {
    return getValue();
  }

  public void setMinutes(int minutes) throws IOException {
    setValue(minutes);
  }
}
