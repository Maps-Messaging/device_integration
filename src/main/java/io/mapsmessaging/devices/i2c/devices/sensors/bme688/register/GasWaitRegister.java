package io.mapsmessaging.devices.i2c.devices.sensors.bme688.register;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.data.GasWait;

import java.io.IOException;

public class GasWaitRegister extends SingleByteRegister {

  private static final int[] MULTIPLICATION_TABLE = {1, 4, 16, 64};

  public GasWaitRegister(I2CDevice sensor, int address, String name) throws IOException {
    super(sensor, address, name);
  }

  public int getTimerSteps() throws IOException {
    reload();
    return registerValue & 0b111111;
  }

  public void setTimerSteps(int value) throws IOException{
    setControlRegister(0b111111, value);
  }

  public int getMultiplicationFactor() throws IOException {
    reload();
    return MULTIPLICATION_TABLE[ (registerValue & 0b11000000) >> 6];
  }

  public void setMultiplicationFactor(int value) throws IOException {
    reload();
    setControlRegister(0b00111111, (value & 0b11) << 6);
  }

  @Override
  public RegisterData toData() throws IOException {
    return new GasWait(getMultiplicationFactor(), getTimerSteps());
  }
}