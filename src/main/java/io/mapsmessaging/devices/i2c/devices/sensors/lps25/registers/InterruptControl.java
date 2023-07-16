package io.mapsmessaging.devices.i2c.devices.sensors.lps25.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.RegisterMap;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;

import java.io.IOException;

public class InterruptControl extends SingleByteRegister {

  private static final byte INTERRUPT_CONTROL = 0x24;
  private static final byte LATCH_INTERRUPT_ENABLE = 0b00000100;
  private static final byte LOW_INTERRUPT_ENABLE = 0b00000010;
  private static final byte HIGH_INTERRUPT_ENABLE = 0b00000001;


  public InterruptControl(I2CDevice sensor, RegisterMap registerMap) throws IOException {
    super(sensor, INTERRUPT_CONTROL, "Interrupt Control", registerMap);
    reload();
  }

  public void setLatchInterruptEnable(boolean flag) throws IOException {
    int value = flag ? LATCH_INTERRUPT_ENABLE : 0;
    setControlRegister(~LATCH_INTERRUPT_ENABLE, value);
  }

  public boolean isLatchInterruptEnabled() throws IOException {
    return (registerValue & LATCH_INTERRUPT_ENABLE) != 0;
  }


  public void setInterruptOnLow(boolean flag) throws IOException {
    int value = flag ? LOW_INTERRUPT_ENABLE : 0;
    setControlRegister(~LOW_INTERRUPT_ENABLE, value);
  }

  public boolean isInterruptOnLowEnabled() throws IOException {
    return (registerValue & LOW_INTERRUPT_ENABLE) != 0;
  }

  public void setInterruptOnHigh(boolean flag) throws IOException {
    int value = flag ? HIGH_INTERRUPT_ENABLE : 0;
    setControlRegister(~HIGH_INTERRUPT_ENABLE, value);
  }

  public boolean isInterruptOnHighEnabled() throws IOException {
    return (registerValue & HIGH_INTERRUPT_ENABLE) != 0;
  }

}