package io.mapsmessaging.devices.i2c.devices.sensors.bme688.measurement;

import io.mapsmessaging.devices.i2c.devices.sensors.bme688.BME688Sensor;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.register.GasReadingRegister;

import java.io.IOException;

public class GasMeasurement implements Measurement {
  private static final int[] GAS_ADDRESSES = {0x2C, 0x3D, 0x4E};

  private final GasReadingRegister gasReadingRegister;

  public GasMeasurement(BME688Sensor sensor, int index, CalibrationData calibrationData)  {
    gasReadingRegister = new GasReadingRegister(sensor, GAS_ADDRESSES[index], "Gas_r_" + index);
  }

  @Override
  public double getMeasurement() throws IOException {
    if (gasReadingRegister.isGasValid()) {
      long gasResAdc = gasReadingRegister.getGasReading();
      int gasRange = gasReadingRegister.getGasRange();
      double var1 = 262144 >> gasRange;
      double var2 = 4096.0 + (gasResAdc - 512)*3;
      return ((10000 * var1)/var2) * 100;
    }
    return Double.NaN;
  }

}
