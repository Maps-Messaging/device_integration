package io.mapsmessaging.devices.i2c.devices.sensors.scd41.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions.PersistSettingsRequest;

public class SensorConfigurationRegister extends RequestRegister {

  private PersistSettingsRequest persistSettingsRequest;

  public SensorConfigurationRegister(I2CDevice sensor) {
    super(sensor, "SensorConfiguration", null);
    this.persistSettingsRequest = new PersistSettingsRequest(sensor.getDevice());
  }

  public void saveSettingsToEEPROM() {
    persistSettingsRequest.getResponse();
  }
}
