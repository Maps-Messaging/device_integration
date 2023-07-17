package io.mapsmessaging.devices.i2c.devices.sensors.msa311.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.values.Odr;

import java.io.IOException;

public class OdrRegister extends SingleByteRegister {

  private static final byte DISABLE_X_AXIS = (byte) 0b10000000;
  private static final byte DISABLE_Y_AXIS = 0b01000000;
  private static final byte DISABLE_Z_AXIS = 0b00100000;

  public OdrRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0x10, "ODR");
  }

  public Odr getOdr() throws IOException {
    reload();
    int val = registerValue & 0b1111;
    for (Odr odr : Odr.values()) {
      if (odr.getMask() == val) {
        return odr;
      }
    }
    return Odr.HERTZ_1000; // Default
  }

  public void setOdr(Odr odr) throws IOException{
    registerValue = (byte) ((registerValue & 0b11110000) | odr.getMask());
    sensor.write(address, registerValue);
  }

  public void disableXAxis(boolean flag) throws IOException {
    int value = flag ? DISABLE_X_AXIS : 0;
    setControlRegister(~DISABLE_X_AXIS, value);
  }

  public boolean isXAxisDisabled(){
    return (registerValue & DISABLE_X_AXIS) != 0;
  }

  public void disableYAxis(boolean flag) throws IOException {
    int value = flag ? DISABLE_Y_AXIS : 0;
    setControlRegister(~DISABLE_Y_AXIS, value);
  }

  public boolean isYAxisDisabled(){
    return (registerValue & DISABLE_Y_AXIS) != 0;
  }

  public void disableZAxis(boolean flag) throws IOException {
    int value = flag ? DISABLE_Z_AXIS : 0;
    setControlRegister(~DISABLE_Z_AXIS, value);
  }

  public boolean isZAxisDisabled(){
    return (registerValue & DISABLE_Z_AXIS) != 0;
  }
}
