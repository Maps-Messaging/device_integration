package io.mapsmessaging.devices.i2c.devices.sensors.scd41.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions.StartPeriodicMeasurementRequest;

public class StartPeriodicMeasurementRegister extends RequestRegister {

  public StartPeriodicMeasurementRegister(I2CDevice sensor) {
    super(sensor, "Start Periodic", new StartPeriodicMeasurementRequest(sensor.getDevice()));
  }

  public void startPeriodicMeasurement(){
    request.getResponse();
  }

}
