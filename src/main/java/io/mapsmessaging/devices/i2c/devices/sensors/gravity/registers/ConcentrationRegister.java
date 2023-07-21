package io.mapsmessaging.devices.i2c.devices.sensors.gravity.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.gravity.config.Command;
import io.mapsmessaging.devices.i2c.devices.sensors.gravity.module.SensorType;

import java.io.IOException;

public class ConcentrationRegister extends CrcValidatingRegsiter {

  public ConcentrationRegister(I2CDevice sensor ) {
    super(sensor, Command.GET_GAS_CONCENTRATION);
  }

  public float getConcentration() throws IOException {
    byte[] data = new byte[9];
    request(new byte[6], data);
    float concentration = (data[2] << 8 | (data[3] & 0xff));
    concentration = adjustPowers(data[5], concentration);
    return concentration;
  }

  public SensorType getSensorType() throws IOException {
    byte[] data = new byte[9];
    if (request(new byte[6], data)) {
      return SensorType.getByType(data[4]);
    }
    return SensorType.UNKNOWN;
  }

}
