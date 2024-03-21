package io.mapsmessaging.devices.i2c.devices.sensors.bme688.register;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.MultiByteRegister;

import java.io.IOException;

public class CalibrationDataRegister extends MultiByteRegister {

  public CalibrationDataRegister(I2CDevice sensor, int address, int size, String name) throws IOException {
    super(sensor, address, size, name);
    reload();
  }


}