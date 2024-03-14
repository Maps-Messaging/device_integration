package io.mapsmessaging.devices.i2c.devices.sensors.bme688.register;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.data.HeaterOn;

import java.io.IOException;

public class ControlGas0Register extends SingleByteRegister {

  public ControlGas0Register(I2CDevice sensor) throws IOException {
    super(sensor, 0x70, "Ctrl_gas_0");
  }

  public boolean isHeatOn() throws IOException {
    reload();
    return (registerValue & 0b1000) != 0;
  }

  public void setHeatOn(boolean flag) throws IOException {
    setControlRegister(0b1000, flag ? 0b1000 : 0);
  }

  @Override
  public RegisterData toData() throws IOException {
    return new HeaterOn(isHeatOn());
  }
}