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
  private final TemperatureCalibrationData temperatureCalibrationData;

  public GasMeasurement(
      BME688Sensor sensor,
      int index,
      GasCalibrationData gasCalibrationData,
      TemperatureCalibrationData temperatureCalibrationData
  )  {
    gasReadingRegister = new GasReadingRegister(sensor, GAS_ADDRESSES[index], "Gas_r_" + index);
    this.gasCalibrationData = gasCalibrationData;
    this.temperatureCalibrationData = temperatureCalibrationData;
  }

  @Override
  public double getMeasurement() throws IOException {
    if (gasReadingRegister.isGasValid()) {
      long gasResAdc = gasReadingRegister.getGasReading();
      int gasRange = gasReadingRegister.getGasRange();
      int parG1 = gasCalibrationData.getParG1();
      int parG2 = gasCalibrationData.getParG2();
      int parG3 = gasCalibrationData.getParG3();




      double var1 = 262144 >> gasRange;
      double var2 = 4096 + (gasResAdc - 512)*3;
      return ((10000 * var1)/var2) * 100;
    }
    return Double.NaN;

    /*
         var1 = 262144 >> self._gas_range
            var2 = self._adc_gas - 512
            var2 *= 3
            var2 = 4096 + var2
            calc_gas_res = (10000 * var1) / var2
            calc_gas_res = calc_gas_res * 100
     */
  }

}
