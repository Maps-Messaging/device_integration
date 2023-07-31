package io.mapsmessaging.devices.i2c.devices.rtc.ds3231.register;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.MultiByteRegister;
import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.data.TemperatureData;

import java.io.IOException;

public class TemperatureRegister extends MultiByteRegister {

  public TemperatureRegister(I2CDevice sensor) {
    super(sensor, 0x11, 2, "TEMPERATURE");
  }

  public float getTemperature() throws IOException {
    reload();
    int tempValue = ((buffer[0] & 0x7F) << 2) + ((buffer[1] >> 6) & 0x03);
    return tempValue / 4.0f;
  }

  @Override
  public RegisterData toData() throws IOException {
    return new TemperatureData(getTemperature());
  }

}