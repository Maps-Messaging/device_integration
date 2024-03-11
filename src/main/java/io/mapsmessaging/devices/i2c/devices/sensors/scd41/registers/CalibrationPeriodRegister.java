package io.mapsmessaging.devices.i2c.devices.sensors.scd41.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions.GetAutoCalibrationInitialPeriod;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions.GetAutoCalibrationStandardPeriod;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions.SetAutoCalibrationInitialPeriod;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions.SetAutoCalibrationStandardPeriod;

public class CalibrationPeriodRegister extends RequestRegister {

  private SetAutoCalibrationStandardPeriod setStandardPeriodRequest;
  private SetAutoCalibrationInitialPeriod setInitialPeriodRequest;
  private GetAutoCalibrationInitialPeriod getInitialPeriodRequest;
  private GetAutoCalibrationStandardPeriod getStandardPeriodRequest;

  public CalibrationPeriodRegister(I2CDevice sensor) {
    super(sensor, "CalibrationPeriod", null); // No default request is associated directly.
    this.setStandardPeriodRequest = new SetAutoCalibrationStandardPeriod(sensor.getDevice());
    this.setInitialPeriodRequest = new SetAutoCalibrationInitialPeriod(sensor.getDevice());
    this.getInitialPeriodRequest = new GetAutoCalibrationInitialPeriod(sensor.getDevice());
    this.getStandardPeriodRequest = new GetAutoCalibrationStandardPeriod(sensor.getDevice());
  }

  public void setStandardPeriod(int period) {
    setStandardPeriodRequest.setPeriod(period);
  }

  public void setInitialPeriod(int period) {
    setInitialPeriodRequest.setPeriod(period);
  }

  public int getInitialPeriod() {
    return getInitialPeriodRequest.getPeriod();
  }

  public int getStandardPeriod() {
    return getStandardPeriodRequest.getPeriod();
  }

}
