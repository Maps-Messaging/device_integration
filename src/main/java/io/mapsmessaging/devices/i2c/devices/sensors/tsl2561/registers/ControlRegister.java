package io.mapsmessaging.devices.i2c.devices.sensors.tsl2561.registers;

import io.mapsmessaging.devices.deviceinterfaces.AbstractRegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.tsl2561.data.ControlData;

import java.io.IOException;

public class ControlRegister extends SingleByteRegister {

  private static final byte POWER_MASK = 0b00000011;

  public ControlRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0x80, "Control");
  }

  public void powerOn() throws IOException {
    setControlRegister(~POWER_MASK, 0b11);
    sensor.delay(500);
  }


  public void powerOff() throws IOException {
    setControlRegister(~POWER_MASK, 0b0);
  }

  @Override
  public AbstractRegisterData toData() throws IOException {
    boolean powerOn = (registerValue & POWER_MASK) == 0b11;
    return new ControlData(powerOn);
  }

  // Method to set ControlRegister data from ControlData
  @Override
  public boolean fromData(AbstractRegisterData input) throws IOException {
    if (input instanceof ControlData) {
      ControlData data = (ControlData) input;
      int value = data.isPowerOn() ? 0b11 : 0b00;
      setControlRegister(~POWER_MASK, value);
      return true;
    }
    return false;
  }

}
