package io.mapsmessaging.devices.i2c.devices.sensors.scd41.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions.SerialNumberRequest;

public class GetSerialNumberRegister  extends RequestRegister {

  public GetSerialNumberRegister(I2CDevice sensor) {
    super(sensor, "Get SerialNo", new SerialNumberRequest(sensor.getDevice()));
  }

  public int isDataReady(){
    return ((SerialNumberRequest)request).getSerialNumber();
  }

}
