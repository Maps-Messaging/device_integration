package io.mapsmessaging.devices.i2c.devices.sensors.scd41.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions.StartLowPowerPeriodicMeasurementRequest;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions.StartPeriodicMeasurementRequest;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions.StopPeriodicMeasurementRequest;

public class PeriodicMeasurementRegister extends RequestRegister {

  private StartPeriodicMeasurementRequest startRequest;
  private StopPeriodicMeasurementRequest stopRequest;
  private StartLowPowerPeriodicMeasurementRequest lowPowerRequest;

  public PeriodicMeasurementRegister(I2CDevice sensor) {
    super(sensor, "PeriodicMeasurement", null);
    this.startRequest = new StartPeriodicMeasurementRequest(sensor.getDevice());
    this.stopRequest = new StopPeriodicMeasurementRequest(sensor.getDevice());
    lowPowerRequest = new StartLowPowerPeriodicMeasurementRequest(sensor.getDevice());
  }

  public void startPeriodicMeasurement() {
    startRequest.getResponse();
  }

  public void startLowPowerMeasurement(){
    lowPowerRequest.getResponse();
  }

  public void stopPeriodicMeasurement() {
    stopRequest.getResponse();
  }
}
