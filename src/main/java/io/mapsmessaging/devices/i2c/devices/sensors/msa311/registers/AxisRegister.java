package io.mapsmessaging.devices.i2c.devices.sensors.msa311.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.RegisterMap;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;

import java.io.IOException;

public class AxisRegister extends SingleByteRegister {

  public AxisRegister(I2CDevice sensor, int startAddress, RegisterMap registerMap) throws IOException {
    super(sensor, startAddress, "Axis", registerMap);
  }

  public float getValue() throws IOException {
    byte[] buffer = new byte[2];
    sensor.readRegister(address, buffer);
    return ((buffer[1] & 0xff) << 4 | ((buffer[0] >> 4) & 0xf));
  }
}
