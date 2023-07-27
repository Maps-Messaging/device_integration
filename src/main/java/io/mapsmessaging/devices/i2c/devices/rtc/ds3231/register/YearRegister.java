package io.mapsmessaging.devices.i2c.devices.rtc.ds3231.register;

import io.mapsmessaging.devices.i2c.I2CDevice;

import java.io.IOException;

public class YearRegister extends BcdRegister {


  public YearRegister(I2CDevice sensor) throws IOException {
    super(sensor,0x6, "YEAR", true);
  }

  public int getYear() throws IOException {
    return 2000+getValue();
  }

  public void setYear(int year) throws IOException {
    setValue(year-2000);
  }
}
