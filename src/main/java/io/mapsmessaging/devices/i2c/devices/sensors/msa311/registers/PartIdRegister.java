package io.mapsmessaging.devices.i2c.devices.sensors.msa311.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.Register;

import java.io.IOException;

public class PartIdRegister extends Register {

  public PartIdRegister(I2CDevice sensor) {
    super(sensor, 0x1);
  }

  public int getId() throws IOException {
    return registerValue & 0xff;
  }
}
