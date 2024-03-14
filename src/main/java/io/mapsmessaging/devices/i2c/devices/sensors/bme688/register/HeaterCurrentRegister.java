package io.mapsmessaging.devices.i2c.devices.sensors.bme688.register;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.MultiByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.data.HeaterCurrent;

import java.io.IOException;

public class HeaterCurrentRegister extends MultiByteRegister {

  public HeaterCurrentRegister(I2CDevice sensor) {
    super(sensor, 0x50, 10, "idac_heat");
  }

  public byte[] getHeaterCurrent() throws IOException {
    reload();
    return super.buffer;
  }

  public void setHeaterCurrent(byte[] val) throws IOException {
    System.arraycopy(val, 0, buffer, 0, buffer.length);
    sensor.write(address, buffer);
  }

  @Override
  public RegisterData toData() throws IOException {
    getHeaterCurrent();
    int[] value = new int[buffer.length];
    for (int x = 0; x < value.length; x++) {
      value[x] = (buffer[x] & 0xff);
    }
    return new HeaterCurrent(value);
  }
}

