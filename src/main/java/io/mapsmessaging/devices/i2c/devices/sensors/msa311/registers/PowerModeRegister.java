package io.mapsmessaging.devices.i2c.devices.sensors.msa311.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.values.LowPowerBandwidth;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.values.PowerMode;

import java.io.IOException;

public class PowerModeRegister extends SingleByteRegister {

  public PowerModeRegister(I2CDevice sensor) {
    super(sensor, 0x11);
  }

  public LowPowerBandwidth getLowPowerBandwidth() throws IOException {
    reload();
    int val = registerValue & 0b1111;
    for (LowPowerBandwidth odr : LowPowerBandwidth.values()) {
      if (val <= odr.getEnd() && val >= odr.getStart()) {
        return odr;
      }
    }
    return LowPowerBandwidth.HERTZ_1_95; // Default
  }

  public void setLowPowerBandwidth(LowPowerBandwidth bandwidth) throws IOException {
    registerValue = (byte) ((registerValue & 0b11000000) | bandwidth.getStart());
    sensor.write(address, registerValue);
  }

  public void setPowerMode(PowerMode mode) throws IOException {
    super.setControlRegister(0b11000000, mode.ordinal() << 6);
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
}
