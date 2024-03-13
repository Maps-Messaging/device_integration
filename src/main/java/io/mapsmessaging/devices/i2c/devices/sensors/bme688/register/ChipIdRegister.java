package io.mapsmessaging.devices.i2c.devices.sensors.bme688.register;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.data.ChipId;

import java.io.IOException;

public class ChipIdRegister extends SingleByteRegister {

  public ChipIdRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0xD0, "Chip_Id");
  }

  public byte getChipId() throws IOException {
    reload();
    return registerValue;
  }

  @Override
  public RegisterData toData() throws IOException {
    return new ChipId(getChipId());
  }
}