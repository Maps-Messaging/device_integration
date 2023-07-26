package io.mapsmessaging.devices.i2c.devices.sensors.lps35.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;

import java.io.IOException;

public class InterruptConfigRegister extends SingleByteRegister {
  
  public InterruptConfigRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0x0B, "INT_CFG");
    reload();
  }
  //region Interrupt Config Register
  public void enableAutoRifp(boolean flag) throws IOException {
    int value = flag ? 0b10000000 : 0;
    setControlRegister( 0b01111111, value);
  }

  public boolean isAutoRifpEnabled() {
    return (registerValue& 0b01000000) != 0;
  }

  public void resetAutoRifp() throws IOException {
    setControlRegister( 0b10111111, 0b01000000);
  }

  public void enableAutoZero(boolean flag) throws IOException {
    int value = flag ? 0b00100000 : 0;
    setControlRegister( 0b11011111, value);
  }

  public boolean isAutoZeroEnabled() {
    return (registerValue& 0b00100000) != 0;
  }

  public void resetAutoZero() throws IOException {
    setControlRegister( 0b11101111, 0b00010000);
  }

  public void enableInterrupt(boolean flag) throws IOException {
    int value = flag ? 0b1000 : 0;
    setControlRegister( 0b11110111, value);
  }

  public boolean isInterruptEnabled(){
    return (registerValue& 0b00001000) != 0;
  }

  public void latchInterruptToSource(boolean flag) throws IOException {
    int value = flag ? 0b100 : 0;
    setControlRegister( 0b11111011, value);
  }

  public boolean isLatchInterruptToSource()  {
    return (registerValue& 0b100) != 0;
  }

  public void latchInterruptToPressureLow(boolean flag) throws IOException {
    int value = flag ? 0b10 : 0;
    setControlRegister( 0b11111101, value);
  }

  public boolean isLatchInterruptToPressureLow(){
    return (registerValue& 0b10) != 0;
  }

  public void latchInterruptToPressureHigh(boolean flag) throws IOException {
    int value = flag ? 0b1 : 0;
    setControlRegister( 0b11111110, value);
  }

  public boolean isLatchInterruptToPressureHigh(){
    return (registerValue& 0b1) != 0;
  }
  
}
