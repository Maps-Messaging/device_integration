package io.mapsmessaging.devices.i2c.devices.sensors.bme688.register;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;

import java.io.IOException;

public class MeasurementStatusRegister extends SingleByteRegister {

  public MeasurementStatusRegister(I2CDevice sensor, int index, String name) throws IOException {
    super(sensor, index, name);
  }

  public boolean hasNewData() {
    return (registerValue & 0b10000000) != 0;
  }

  public boolean isReadingGas() {
    return (registerValue & 0b01000000) != 0;
  }

  public boolean isMeasuring() {
    return (registerValue & 0b00100000) != 0;
  }

  public int getGasMeasureIndex() {
    return (registerValue & 0b1111);
  }

}