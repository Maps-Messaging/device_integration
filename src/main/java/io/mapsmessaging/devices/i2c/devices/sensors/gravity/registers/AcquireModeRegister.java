package io.mapsmessaging.devices.i2c.devices.sensors.gravity.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.gravity.config.Command;

import java.io.IOException;

public class AcquireModeRegister extends CrcValidatingRegister {

  public AcquireModeRegister(I2CDevice sensor) {
    super(sensor, Command.CHANGE_GET_METHOD);
  }

  public boolean setI2CGroup(byte group) throws IOException {
    return super.simpleRequest(group);
  }

}
