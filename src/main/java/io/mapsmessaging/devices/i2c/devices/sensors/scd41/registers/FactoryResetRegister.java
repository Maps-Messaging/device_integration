package io.mapsmessaging.devices.i2c.devices.sensors.scd41.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions.FactoryResetRequest;

public class FactoryResetRegister extends RequestRegister {

  public FactoryResetRegister(I2CDevice sensor) {
    super(sensor, "FactoryReset", new FactoryResetRequest(sensor.getDevice()));
  }

  public void factoryResetDevice(){
    request.getResponse();
  }

}
