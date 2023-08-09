package io.mapsmessaging.devices.i2c.devices.rtc.ds3231.register;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.data.SecondData;

import java.io.IOException;

public class SecondsRegister extends BcdRegister {


  public SecondsRegister(I2CDevice sensor, int address, String name) throws IOException {
    super(sensor, address, name, false);
  }

  public int getSeconds() throws IOException {
    return getValue();
  }

  public void setSeconds(int seconds) throws IOException {
    setValue(seconds);
  }

  @Override
  public boolean fromData(RegisterData input) throws IOException {
    if (input instanceof SecondData) {
      SecondData data = (SecondData) input;
      setSeconds(data.getSeconds());
      return true;
    }
    return false;
  }

  @Override
  public RegisterData toData() throws IOException {
    return new SecondData(getSeconds());
  }
}
