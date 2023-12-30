package io.mapsmessaging.devices.i2c.devices.rtc.ds3231.register;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.data.MonthDayData;

import java.io.IOException;

public class MonthDayRegister extends BcdRegister {
  private static final int DATE = 0b00001111;
  private static final int TEN_DATE = 0b00110000;

  public MonthDayRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0x4, "DATE", false);
  }

  public int getDate() throws IOException {
    reload();
    return super.getValue();
  }

  public void setDate(int date) throws IOException {
    super.setValue(date);
  }

  @Override
  public boolean fromData(RegisterData input) throws IOException {
    if (input instanceof MonthDayData) {
      MonthDayData data = (MonthDayData) input;
      setDate(data.getDate());
      return true;
    }
    return false;
  }

  @Override
  public RegisterData toData() throws IOException {
    return new MonthDayData(getDate());
  }

}
