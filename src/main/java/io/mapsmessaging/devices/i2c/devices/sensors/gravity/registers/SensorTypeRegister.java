package io.mapsmessaging.devices.i2c.devices.sensors.gravity.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.gravity.config.Command;
import io.mapsmessaging.devices.i2c.devices.sensors.gravity.module.SensorType;

import java.io.IOException;

public class SensorTypeRegister  extends CrcValidatingRegsiter {

  public SensorTypeRegister(I2CDevice sensor ) {
    super(sensor, Command.GET_GAS_CONCENTRATION);
  }

  public SensorType getSensorType() throws IOException {
    byte[] data = new byte[9];
    if (request(new byte[6], data)) {
      return SensorType.getByType(data[4]);
    }
    return SensorType.UNKNOWN;
  }

}
