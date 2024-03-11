package io.mapsmessaging.devices.i2c.devices.sensors.scd41.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions.WakeUpRequest;

public class WakeUpRegister  extends RequestRegister {

  public WakeUpRegister(I2CDevice sensor) {
    super(sensor, "Wake Up", new WakeUpRequest(sensor.getDevice()));
  }

  public void wakeUp(){
    request.getResponse();
  }

}