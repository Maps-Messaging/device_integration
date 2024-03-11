package io.mapsmessaging.devices.i2c.devices.sensors.scd41.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions.DataReadyRequest;


public class DataReadyRegister extends RequestRegister {

  public DataReadyRegister(I2CDevice sensor) {
    super(sensor, "DataReady", new DataReadyRequest(sensor.getDevice()));
  }

  public boolean isDataReady(){
    return ((DataReadyRequest)request).isDataReady();
  }

}
