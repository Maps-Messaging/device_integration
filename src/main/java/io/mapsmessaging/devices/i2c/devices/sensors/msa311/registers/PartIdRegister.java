package io.mapsmessaging.devices.i2c.devices.sensors.msa311.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;

import java.io.IOException;

public class PartIdRegister extends SingleByteRegister {

  public PartIdRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0x1, "PartId");
  }

  public int getId() throws IOException {
    return registerValue & 0xff;
  }
}
