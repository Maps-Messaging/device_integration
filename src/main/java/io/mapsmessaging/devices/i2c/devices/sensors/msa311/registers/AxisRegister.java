package io.mapsmessaging.devices.i2c.devices.sensors.msa311.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.MultiByteRegister;

import java.io.IOException;

public class AxisRegister extends MultiByteRegister {

  public AxisRegister(I2CDevice sensor, int startAddress, String name) throws IOException {
    super(sensor, startAddress, 2, name);
  }

  public int getValue() throws IOException {
    reload();
    int val = (int) asSignedLong();
    return val >> 2; // This should support msa301
  }

}
