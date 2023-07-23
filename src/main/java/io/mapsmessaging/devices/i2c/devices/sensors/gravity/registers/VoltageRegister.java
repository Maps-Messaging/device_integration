package io.mapsmessaging.devices.i2c.devices.sensors.gravity.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.gravity.config.Command;

import java.io.IOException;

public class VoltageRegister extends CrcValidatingRegister {

  public VoltageRegister(I2CDevice sensor) {
    super(sensor, Command.SENSOR_VOLTAGE);
  }

  public float getVoltage() throws IOException {
    byte[] recvbuf = new byte[9];
    if (request(new byte[6], recvbuf)) {
      return ((recvbuf[2] << 8 | recvbuf[3] & 0xff) * 3.0f) / 1024.0f * 2f;
    }
    return Float.NaN;
  }

}

