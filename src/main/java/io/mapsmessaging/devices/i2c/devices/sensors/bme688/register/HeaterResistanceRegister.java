package io.mapsmessaging.devices.i2c.devices.sensors.bme688.register;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.MultiByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.data.HeatResistance;

import java.io.IOException;

public class HeaterResistanceRegister extends MultiByteRegister {

  public HeaterResistanceRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0x5A, 10, "res_heat");
    reload();
  }

  public byte[] getHeaterResistance()  {
    return super.buffer;
  }

  public void setHeaterResistance(int idx, byte val) throws IOException {
    buffer[idx] = val;
    sensor.write(address+idx, val);
  }

  @Override
  public RegisterData toData() throws IOException {
    getHeaterResistance();
    int[] value = new int[buffer.length];
    for (int x = 0; x < value.length; x++) {
      value[x] = (buffer[x] & 0xff);
    }
    return new HeatResistance(value);
  }
}

