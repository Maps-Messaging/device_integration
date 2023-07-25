package io.mapsmessaging.devices.i2c.devices.sensors.tsl2561.registers;

import io.mapsmessaging.devices.deviceinterfaces.AbstractRegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.tsl2561.data.InterruptControlData;
import io.mapsmessaging.devices.i2c.devices.sensors.tsl2561.values.InterruptControl;
import io.mapsmessaging.devices.i2c.devices.sensors.tsl2561.values.InterruptPersistence;

import java.io.IOException;

public class InterruptControlRegister extends SingleByteRegister {

  private static final byte INTR_MASK = 0b00110000;
  private static final byte PERSIST_MASK = 0b00001111;

  public InterruptControlRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0x86, "Interrupt Control");
    reload();
  }

  public void setControl(InterruptControl control) throws IOException {
    setControlRegister(INTR_MASK, control.ordinal());
  }

  public InterruptControl getControl(){
    int val = (registerValue &INTR_MASK) >> 4;
    return InterruptControl.values()[val];
  }


  public void setPersist(InterruptPersistence persist) throws IOException {
    setControlRegister(PERSIST_MASK, persist.ordinal());
  }

  public InterruptPersistence getPersist(){
    int val = (registerValue & PERSIST_MASK);
    return InterruptPersistence.values()[val];
  }

  @Override
  public AbstractRegisterData toData() throws IOException {
    InterruptControl control = getControl();
    InterruptPersistence persist = getPersist();
    return new InterruptControlData(control, persist);
  }

  // Method to set InterruptControlRegister data from InterruptControlData
  @Override
  public boolean fromData(AbstractRegisterData input) throws IOException {
    if (input instanceof InterruptControlData) {
      InterruptControlData data = (InterruptControlData) input;
      setControl(data.getControl());
      setPersist(data.getPersist());
      return true;
    }
    return false;
  }
}
