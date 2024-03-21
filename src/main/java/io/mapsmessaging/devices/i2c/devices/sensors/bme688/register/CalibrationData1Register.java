package io.mapsmessaging.devices.i2c.devices.sensors.bme688.register;

import io.mapsmessaging.devices.i2c.I2CDevice;

import java.io.IOException;

public class CalibrationData1Register extends CalibrationDataRegister {

  public CalibrationData1Register(I2CDevice sensor) throws IOException {
    super(sensor, 0x8A, 23, "Coeff1");
  }

}