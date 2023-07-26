package io.mapsmessaging.devices.i2c.devices.sensors.tsl2561.registers;

import io.mapsmessaging.devices.deviceinterfaces.AbstractRegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.tsl2561.data.TimingData;
import io.mapsmessaging.devices.i2c.devices.sensors.tsl2561.values.IntegrationTime;

import java.io.IOException;

public class TimingRegister extends SingleByteRegister {

  private static final byte GAIN_MASK = 0b00010000;
  private static final byte MANUAL_MASK = 0b00001000;
  private static final byte INTEGRATION_MASK = 0b00000011;


  public TimingRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0x81, "Timing");
    reload();
  }

  public boolean getManual() {
    return (registerValue & MANUAL_MASK) != 0;
  }

  public void setManual(boolean flag) throws IOException {
    if (flag) {
      registerValue |= MANUAL_MASK;
    } else {
      registerValue &= ~MANUAL_MASK;
    }
    sensor.write(address, registerValue);
  }

  public boolean getHighGain() {
    return (registerValue & GAIN_MASK) != 0;
  }

  public void setHighGain(boolean flag) throws IOException {
    if (flag) {
      registerValue |= GAIN_MASK;
    } else {
      registerValue &= ~GAIN_MASK;
    }
    sensor.write(address, registerValue);
  }

  public IntegrationTime getIntegrationTime() {
    byte val = (byte) (registerValue & INTEGRATION_MASK);
    for (IntegrationTime time : IntegrationTime.values()) {
      if (time.getMask() == val) {
        return time;
      }
    }
    return IntegrationTime.MANUAL;
  }

  public void setIntegrationTime(IntegrationTime times) throws IOException {
    byte mask = times.getMask();
    super.setControlRegister(INTEGRATION_MASK, mask);
    sensor.delay(500);
  }

  @Override
  public AbstractRegisterData toData() throws IOException {
    boolean manual = getManual();
    boolean highGain = getHighGain();
    IntegrationTime integrationTime = getIntegrationTime();
    return new TimingData(manual, highGain, integrationTime);
  }

  // Method to set TimingRegister data from TimingData
  @Override
  public boolean fromData(AbstractRegisterData input) throws IOException {
    if (input instanceof TimingData) {
      TimingData data = (TimingData) input;
      setManual(data.isManual());
      setHighGain(data.isHighGain());
      setIntegrationTime(data.getIntegrationTime());
      return true;
    }
    return false;
  }

}