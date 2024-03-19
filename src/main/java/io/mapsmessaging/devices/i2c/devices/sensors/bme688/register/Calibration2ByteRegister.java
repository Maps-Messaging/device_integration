package io.mapsmessaging.devices.i2c.devices.sensors.bme688.register;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.MultiByteRegister;

import java.io.IOException;

public class Calibration2ByteRegister extends MultiByteRegister {

  public Calibration2ByteRegister(I2CDevice sensor, int address, String name) {
    super(sensor, address, 2, name);
  }

  public int getValue() throws IOException {
    reload();
    return ((buffer[1] & 0xff)<< 8) | ((buffer[0] & 0xff));
  }

}