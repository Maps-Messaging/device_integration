package io.mapsmessaging.devices.i2c.devices.sensors.bme688.register;

import io.mapsmessaging.devices.i2c.I2CDevice;

import java.io.IOException;

public class CalibrationData2Register  extends CalibrationDataRegister {

  public CalibrationData2Register(I2CDevice sensor) throws IOException {
    super(sensor, 0xE1, 14, "Coeff2");
  }


}