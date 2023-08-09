package io.mapsmessaging.devices.i2c.devices.rtc.ds3231.register;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.data.WeekDayData;

import java.io.IOException;

public class WeekDayRegister extends SingleByteRegister {
  private static final int DAY = 0b00000111;

  public WeekDayRegister(I2CDevice sensor) throws IOException {
    super(sensor, 3, "DAY");
  }

  public int getDay() throws IOException {
    reload();
    return registerValue & DAY;
  }

  public void setDay(int day) throws IOException {
    super.setControlRegister(~DAY, day & DAY);
  }

  @Override
  public boolean fromData(RegisterData input) throws IOException {
    if (input instanceof WeekDayData) {
      WeekDayData data = (WeekDayData) input;
      setDay(data.getDay());
      return true;
    }
    return false;
  }

  @Override
  public RegisterData toData() throws IOException {
    return new WeekDayData(getDay());
  }
}
