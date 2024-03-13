package io.mapsmessaging.devices.i2c.devices.sensors.bme688.register;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.data.VariantId;

import java.io.IOException;

public class VariantIdRegister extends SingleByteRegister {

  public VariantIdRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0xF0, "Variant_Id");
  }

  public byte getVariantId() throws IOException {
    reload();
    return registerValue;
  }

  @Override
  public RegisterData toData() throws IOException {
    return new VariantId(getVariantId());
  }
}