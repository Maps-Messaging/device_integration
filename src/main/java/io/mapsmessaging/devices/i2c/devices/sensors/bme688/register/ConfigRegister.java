package io.mapsmessaging.devices.i2c.devices.sensors.bme688.register;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.data.ConfigData;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.values.FilterSize;

import java.io.IOException;

public class ConfigRegister extends SingleByteRegister {

  public ConfigRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0x75, "Config");
  }

  public FilterSize getFilterSize() throws IOException {
    reload();
    return FilterSize.values()[(registerValue >> 2) & 0b111];
  }

  public void setFilterSize(FilterSize filterSize) throws IOException {
    int val = filterSize.getValue();
    setControlRegister(0b11100011, (byte)((val&0b111)<<2));
  }

  @Override
  public RegisterData toData() throws IOException {
    return new ConfigData(getFilterSize());
  }
}