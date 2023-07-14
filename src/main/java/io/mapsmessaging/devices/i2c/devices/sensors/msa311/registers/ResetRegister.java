package io.mapsmessaging.devices.i2c.devices.sensors.msa311.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.Register;

import java.io.IOException;

public class ResetRegister extends Register {

  public ResetRegister(I2CDevice sensor) {
    super(sensor, 0x0);
  }

  public void reset() throws IOException {
    sensor.write(address, (byte)0b0100100);
  }
}
