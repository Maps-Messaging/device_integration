package io.mapsmessaging.devices.i2c.devices.sensors.bme688.register;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.MultiByteRegister;

import java.io.IOException;

public class LargeValueRegister extends MultiByteRegister {

  public LargeValueRegister(I2CDevice sensor, int address, String name) {
    super(sensor, address, 3, name);
  }

  public int getValue() throws IOException {
    reload();
    return (buffer[0] << 12) | ((buffer[1] & 0xff) << 4) | ((buffer[2] & 0xff) >> 4);
  }
}
