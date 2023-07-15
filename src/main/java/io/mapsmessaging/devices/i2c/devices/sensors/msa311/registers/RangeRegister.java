package io.mapsmessaging.devices.i2c.devices.sensors.msa311.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.values.Range;

import java.io.IOException;

public class RangeRegister extends SingleByteRegister {

  public RangeRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0xF);
  }

  public Range getRange() {
    int val = (registerValue & 0b11);
    for (Range range : Range.values()) {
      if (range.ordinal() == val) {
        return range;
      }
    }
    return Range.RANGE_2G;
  }

  public void setRange(Range range) throws IOException {
    registerValue = (byte) (range.ordinal() & 0b11);
    sensor.write(address, registerValue);
  }

}
