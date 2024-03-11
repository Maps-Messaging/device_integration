package io.mapsmessaging.devices.i2c.devices.sensors.scd41.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions.PowerDownRequest;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions.WakeUpRequest;

public class PowerManagementRegister extends RequestRegister {

  private PowerDownRequest powerDownRequest;
  private WakeUpRequest wakeUpRequest;

  public PowerManagementRegister(I2CDevice sensor) {
    // Initially, no request is directly associated; both requests are managed independently
    super(sensor, "Power Management", null);
    this.powerDownRequest = new PowerDownRequest(sensor.getDevice());
    this.wakeUpRequest = new WakeUpRequest(sensor.getDevice());
  }

  public void powerDown() {
    // Trigger the power down action
    powerDownRequest.getResponse(); // Assuming getResponse() initiates the request's action
  }

  public void wakeUp() {
    // Trigger the wake up action
    wakeUpRequest.getResponse(); // Similarly, assuming getResponse() initiates the request
  }
}
