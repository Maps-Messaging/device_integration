package io.mapsmessaging.devices.i2c.devices.sensors.lps35.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;

import java.io.IOException;

public class InterruptConfigRegister extends SingleByteRegister {

  private static final int AUTO_RIFP = 0b10000000;
  private static final int RESET_ARP = 0b01000000;
  private static final int AUTO_ZERO = 0b00100000;
  private static final int RESET_AZ = 0b00010000;
  private static final int DIFF_EN = 0b00001000;
  private static final int LIR = 0b00000100;
  private static final int PLE = 0b00000010;
  private static final int PHE = 0b00000001;

  public InterruptConfigRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0x0B, "INTERRUPT_CFG");
    reload();
  }

  //region Interrupt Config Register
  public void enableAutoRifp(boolean flag) throws IOException {
    setControlRegister(AUTO_RIFP, flag ? AUTO_RIFP : 0);
  }

  public boolean isAutoRifpEnabled() {
    return (registerValue & AUTO_RIFP) != 0;
  }

  public void resetAutoRifp() throws IOException {
    setControlRegister(RESET_ARP, RESET_ARP);
  }

  public void enableAutoZero(boolean flag) throws IOException {
    setControlRegister(AUTO_ZERO, flag ? AUTO_ZERO : 0);
  }

  public boolean isAutoZeroEnabled() {
    return (registerValue & AUTO_ZERO) != 0;
  }

  public void resetAutoZero() throws IOException {
    setControlRegister(RESET_AZ, RESET_AZ);
  }

  public void enableInterrupt(boolean flag) throws IOException {
    setControlRegister(DIFF_EN, flag ? DIFF_EN : 0);
  }

  public boolean isInterruptEnabled() {
    return (registerValue & DIFF_EN) != 0;
  }

  public void latchInterruptToSource(boolean flag) throws IOException {
    setControlRegister(LIR, flag ? LIR : 0);
  }

  public boolean isLatchInterruptToSource() {
    return (registerValue & LIR) != 0;
  }

  public void latchInterruptToPressureLow(boolean flag) throws IOException {
    setControlRegister(PLE, flag ? PLE : 0);
  }

  public boolean isLatchInterruptToPressureLow() {
    return (registerValue & PLE) != 0;
  }

  public void latchInterruptToPressureHigh(boolean flag) throws IOException {
    int value = flag ? PHE : 0;
    setControlRegister(PHE, flag ? PHE : 0);
  }

  public boolean isLatchInterruptToPressureHigh() {
    return (registerValue & PHE) != 0;
  }

}
