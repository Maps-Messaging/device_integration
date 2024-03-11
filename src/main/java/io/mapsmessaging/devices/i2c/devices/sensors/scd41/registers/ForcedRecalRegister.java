package io.mapsmessaging.devices.i2c.devices.sensors.scd41.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions.FactoryResetRequest;

public class ForcedRecalRegister  extends RequestRegister {

  public ForcedRecalRegister(I2CDevice sensor) {
    super(sensor, "Force Recal", new FactoryResetRequest(sensor.getDevice()));
  }

  public void forceRecallibration(){
    request.getResponse();
  }

}
