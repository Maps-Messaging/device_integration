package io.mapsmessaging.devices.i2c.devices.sensors.bme688.register;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.MultiByteRegister;

import java.io.IOException;

public class GasReadingRegister extends MultiByteRegister {

  public GasReadingRegister(I2CDevice sensor, int address, String name) {
    super(sensor, address, 2, name);
  }

  public int getGasReading() throws IOException {
    reload();
    return ((buffer[1] & 0b11000000) >> 6) | ((buffer[0] & 0xff)<<2);
  }

  public int getGasRange() {
    return buffer[1] & 0b1111;
  }

  public boolean isGasValid() throws IOException {
    reload();
    return (buffer[1] & 0b100000) != 0;
  }

  public boolean isHeatStable() {
    return (buffer[1] & 0b10000) != 0;
  }
}
