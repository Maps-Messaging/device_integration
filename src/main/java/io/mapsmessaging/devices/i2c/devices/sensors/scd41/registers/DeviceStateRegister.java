package io.mapsmessaging.devices.i2c.devices.sensors.scd41.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions.FactoryResetRequest;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions.ReInitRequest;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions.SelfTestRequest;

public class DeviceStateRegister extends RequestRegister {

  private SelfTestRequest selfTestRequest;
  private FactoryResetRequest factoryResetRequest;
  private ReInitRequest reInitRequest;

  public DeviceStateRegister(I2CDevice sensor) {
    super(sensor, "Device State Management", null);
    this.selfTestRequest = new SelfTestRequest(sensor.getDevice());
    this.factoryResetRequest = new FactoryResetRequest(sensor.getDevice());
    this.reInitRequest = new ReInitRequest(sensor.getDevice());
  }

  public void performSelfTest() {
    selfTestRequest.getResponse();
  }

  public void factoryReset() {
    factoryResetRequest.getResponse();
  }

  public void reInitialize() {
    reInitRequest.getResponse();
  }
}
