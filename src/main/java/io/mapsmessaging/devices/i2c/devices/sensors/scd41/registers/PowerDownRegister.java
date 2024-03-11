package io.mapsmessaging.devices.i2c.devices.sensors.scd41.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions.PowerDownRequest;

public class PowerDownRegister extends RequestRegister {

  public PowerDownRegister(I2CDevice sensor) {
    super(sensor, "Power Down", new PowerDownRequest(sensor.getDevice()));
  }

  public void powerDown(){
    request.getResponse();
  }

}