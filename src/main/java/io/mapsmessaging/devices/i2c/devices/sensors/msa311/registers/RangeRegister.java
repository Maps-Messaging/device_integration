package io.mapsmessaging.devices.i2c.devices.sensors.msa311.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.Register;

import java.io.IOException;

public class RangeRegister extends Register {

  public RangeRegister(I2CDevice sensor) {
    super(sensor, 0xF);
  }

  public int getRange() throws IOException {
    reload();
    return (registerValue & 0b11);
  }

  public void setRange(int range) throws IOException {
    registerValue = (byte)(range & 0b11);
    sensor.write(address, registerValue);
  }

}
