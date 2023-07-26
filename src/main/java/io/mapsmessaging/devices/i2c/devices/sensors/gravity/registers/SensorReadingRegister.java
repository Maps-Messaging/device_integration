package io.mapsmessaging.devices.i2c.devices.sensors.gravity.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.gravity.config.Command;

import java.io.IOException;

public class SensorReadingRegister extends CrcValidatingRegister {

  private float concentration;
  private float temperature;
  private long lastRead;

  public SensorReadingRegister(I2CDevice sensor) {
    super(sensor, Command.GET_ALL_DATA);
    lastRead = 0;
  }

  public float getConcentration() throws IOException {
    updateAllFields();
    return concentration;
  }

  public float getTemperature() throws IOException {
    updateAllFields();
    return temperature;
  }


  private void updateAllFields() throws IOException {
    if (lastRead < System.currentTimeMillis()) {
      byte[] data = new byte[9];
      if (request(new byte[6], data)) {
        concentration = (data[2] << 8 | (data[3] & 0xff));
        concentration = adjustPowers(data[5], concentration);
        int raw = data[6] << 8 | (data[7] & 0xff);
        temperature = computeTemperature(raw);
      }
      lastRead = System.currentTimeMillis() + 100;
    }
  }

}
