package io.mapsmessaging.devices.i2c.devices.sensors.msa311.registers;

import io.mapsmessaging.devices.deviceinterfaces.AbstractRegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.data.PowerModeData;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.values.LowPowerBandwidth;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.values.PowerMode;

import java.io.IOException;

public class PowerModeRegister extends SingleByteRegister {

  public PowerModeRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0x11, "Power_Mode");
  }

  public LowPowerBandwidth getLowPowerBandwidth() throws IOException {
    reload();
    int val = (registerValue & 0b11110) >> 1;
    for (LowPowerBandwidth odr : LowPowerBandwidth.values()) {
      if (val <= odr.getEnd() && val >= odr.getStart()) {
        return odr;
      }
    }
    return LowPowerBandwidth.HERTZ_1_95; // Default
  }

  public void setLowPowerBandwidth(LowPowerBandwidth bandwidth) throws IOException {
    registerValue = (byte) ((registerValue & 0b11000001) | (bandwidth.getStart() << 1));
    sensor.write(address, registerValue);
  }

  public void setPowerMode(PowerMode mode) throws IOException {
    super.setControlRegister(0b00011110, mode.ordinal() << 6);
  }

  public PowerMode getPowerMode() throws IOException {
    int val = registerValue >> 6;
    for (PowerMode mode : PowerMode.values()) {
      if (mode.ordinal() == val) {
        return mode;
      }
    }
    return PowerMode.UNKNOWN;
  }
  public AbstractRegisterData toData() throws IOException {
    return new PowerModeData(getLowPowerBandwidth(), getPowerMode());
  }

  @Override
  public boolean fromData(AbstractRegisterData input) throws IOException {
    if (input instanceof PowerModeData) {
      PowerModeData data = (PowerModeData) input;
      setLowPowerBandwidth(data.getLowPowerBandwidth());
      setPowerMode(data.getPowerMode());
      return true;
    }
    return false;
  }

}
