package io.mapsmessaging.devices.i2c.devices.sensors.bme688.register;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;

import java.io.IOException;

public class ResetRegister extends SingleByteRegister {

  public ResetRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0XE0, "Reset");
  }

  public void reset() throws IOException {
    setControlRegister(0b11111111, 0xB6);
  }

}