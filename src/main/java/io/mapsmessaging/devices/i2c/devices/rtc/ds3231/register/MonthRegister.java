package io.mapsmessaging.devices.i2c.devices.rtc.ds3231.register;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.data.MonthData;

import java.io.IOException;

public class MonthRegister extends BcdRegister {

  private static final int CENTURY = 0b10000000;

  private static final int MONTH_MASK = 0b00011111;
  private static final int TEN_MONTH = 0b00010000;
  private static final int MONTH = 0b00001111;

  public MonthRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0x5, "MONTH", false);
    reload();
  }

  public boolean isCentury() {
    return (registerValue & CENTURY) != 0;
  }

  public void setCentury(boolean flag) throws IOException {
    setControlRegister(~CENTURY, flag ? CENTURY : 0);
  }

  public int getMonth() throws IOException {
    reload();
    return super.getValue();
  }

  public void setMonth(int val) throws IOException {
    super.setValue(val);
  }

  @Override
  public boolean fromData(RegisterData input) throws IOException {
    if (input instanceof MonthData) {
      MonthData data = (MonthData) input;
      setMonth(data.getMonth());
      setCentury(data.isCentury());
      return true;
    }
    return false;
  }

  @Override
  public RegisterData toData() throws IOException {
    return new MonthData(getMonth(), isCentury());
  }
}

