package io.mapsmessaging.devices.i2c.devices.sensors.gravity.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.gravity.config.Command;

import java.io.IOException;

public class TemperatureRegister extends CrcValidatingRegister {

  public TemperatureRegister(I2CDevice sensor) {
    super(sensor, Command.GET_TEMP);
  }

  public float getTemperature() throws IOException {
    byte[] data = new byte[9];
    if (request(new byte[6], data)) {
      int raw = data[2] << 8 | (data[3] & 0xff);
      return computeTemperature(raw);
    }
    return Float.NaN;
  }

}
