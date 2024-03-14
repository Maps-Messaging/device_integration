package io.mapsmessaging.devices.i2c.devices.sensors.bme688.measurement;

import io.mapsmessaging.devices.i2c.devices.sensors.bme688.BME688Sensor;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.register.GasReadingRegister;

import java.io.IOException;

public class GasMeasurement implements Measurement {

  private static final int[] GAS_ADDRESSES = {0x2C, 0x3D, 0x4E};

  private final GasReadingRegister gasReadingRegister;

  public GasMeasurement(BME688Sensor sensor, int index) throws IOException {
    gasReadingRegister = new GasReadingRegister(sensor, GAS_ADDRESSES[index], "Gas_r_" + index);
  }

  @Override
  public double getMeasurement() {
    return 0;
  }
}
