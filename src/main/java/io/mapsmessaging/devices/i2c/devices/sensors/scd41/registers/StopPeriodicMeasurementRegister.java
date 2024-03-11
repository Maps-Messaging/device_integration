package io.mapsmessaging.devices.i2c.devices.sensors.scd41.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions.StopPeriodicMeasurementRequest;

public class StopPeriodicMeasurementRegister  extends RequestRegister {

  public StopPeriodicMeasurementRegister(I2CDevice sensor) {
    super(sensor, "Stop Periodic", new StopPeriodicMeasurementRequest(sensor.getDevice()));
  }

  public void stopPeriodicMeasurement(){
    request.getResponse();
  }

}
