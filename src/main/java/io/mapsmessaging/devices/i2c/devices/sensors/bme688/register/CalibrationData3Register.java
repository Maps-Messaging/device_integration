package io.mapsmessaging.devices.i2c.devices.sensors.bme688.register;

import io.mapsmessaging.devices.i2c.I2CDevice;

import java.io.IOException;

public class CalibrationData3Register  extends CalibrationDataRegister {

  public CalibrationData3Register(I2CDevice sensor) throws IOException {
    super(sensor, 0x0, 5, "Coeff3");
  }

}