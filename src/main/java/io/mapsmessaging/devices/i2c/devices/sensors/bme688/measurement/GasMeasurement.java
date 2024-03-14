package io.mapsmessaging.devices.i2c.devices.sensors.bme688.measurement;

import io.mapsmessaging.devices.i2c.devices.sensors.bme688.BME688Sensor;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.register.GasReadingRegister;

import java.io.IOException;

public class GasMeasurement implements Measurement {

  private static final double[] lookupK1Range = {0.0, 0.0, 0.0, 0.0, 0.0, -1.0, 0.0, -0.8, 0.0, 0.0, -0.2, -0.5, 0.0, -1.0, 0.0, 0.0};
  private static final double[] lookupK2Range = {0.0, 0.0, 0.0, 0.0, 0.1, 0.7, 0.0, -0.8, -0.1, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
  private static final int[] GAS_ADDRESSES = {0x2C, 0x3D, 0x4E};

  private final GasCalibrationData gasCalibrationData;
  private final GasReadingRegister gasReadingRegister;

  public GasMeasurement(BME688Sensor sensor, int index, GasCalibrationData gasCalibrationData) throws IOException {
    gasReadingRegister = new GasReadingRegister(sensor, GAS_ADDRESSES[index], "Gas_r_" + index);
    this.gasCalibrationData = gasCalibrationData;
  }

  @Override
  public double getMeasurement() throws IOException {
    if (gasReadingRegister.isGasValid()) {
      long gasResAdc = gasReadingRegister.getGasReading();
      int gasRange = gasReadingRegister.getGasRange();
      double var1;
      double var2;
      double var3;
      double gasRangeF = (1 << gasRange);
      var1 = (1340.0 + (5.0 * gasCalibrationData.getParG1()));
      var2 = var1 * (1.0 + lookupK1Range[gasRange] / 100.0);
      var3 = 1.0 + (lookupK2Range[gasRange] / 100.0);
      return 1.0 / (var3 * 0.000000125 * gasRangeF * (((gasResAdc - 512.0) / var2) + 1.0));
    }
    return Double.NaN;
  }

}
