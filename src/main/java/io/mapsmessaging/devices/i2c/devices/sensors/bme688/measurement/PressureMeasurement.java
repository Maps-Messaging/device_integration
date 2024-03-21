package io.mapsmessaging.devices.i2c.devices.sensors.bme688.measurement;

import io.mapsmessaging.devices.i2c.devices.sensors.bme688.BME688Sensor;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.register.LargeValueRegister;

import java.io.IOException;

public class PressureMeasurement implements Measurement {

  private static final int[] PRESSURE_ADDRESSES = {0x1F, 0x30, 0x41};

  private final LargeValueRegister pressureMeasurementRegister;
  private final PressureCalibrationData pressureCalibrationData;
  private final TemperatureCalibrationData temperatureCalibrationData;

  public PressureMeasurement(BME688Sensor sensor,
                             int index,
                             CalibrationData calibrationData) {
    pressureMeasurementRegister = new LargeValueRegister(sensor, PRESSURE_ADDRESSES[index], "pres_adc_" + index);
    this.pressureCalibrationData = calibrationData.getPressureCalibrationData();
    this.temperatureCalibrationData = calibrationData.getTemperatureCalibrationData();
  }

  @Override
  public double getMeasurement() throws IOException {
    int presAdc = pressureMeasurementRegister.getValue(); // Ensure this handles the sign correctly if needed
    int parP1 = pressureCalibrationData.getParP1();
    int parP2 = pressureCalibrationData.getParP2();
    int parP3 = pressureCalibrationData.getParP3();
    int parP4 = pressureCalibrationData.getParP4();
    int parP5 = pressureCalibrationData.getParP5();
    int parP6 = pressureCalibrationData.getParP6();
    int parP7 = pressureCalibrationData.getParP7();
    int parP8 = pressureCalibrationData.getParP8();
    int parP9 = pressureCalibrationData.getParP9();
    int parP10 = pressureCalibrationData.getParP10();
    int tFine = temperatureCalibrationData.getTFine();

    final int presOvfCheck = 0x40000000;
    // Assuming CalibrationData is a class that holds calibration values.
    int var1 = (((tFine) >> 1) - 64000);
    int var2 = ((((var1 >> 2) * (var1 >> 2)) >> 11) * parP6) >> 2;
    var2 = var2 + ((var1 * parP5) << 1);
    var2 = (var2 >> 2) + (parP4 << 16);
    var1 = (((var1 >> 2) * (var1 >> 2)) >> 13) * (parP3 << 5);
    var1 = ((var1 >> 3) + ((parP2 * var1) >> 1)) >> 18;
    var1 = (((32768 + var1) * parP1) >> 15);
    int  pressureComp = 1048576 - presAdc;
    pressureComp = ((pressureComp - (var2 >> 12)) * (3125));
    if (pressureComp < presOvfCheck) {
      pressureComp = (pressureComp << 1) / var1;
    } else {
      pressureComp = (pressureComp / var1) << 1;
    }

    int var3 = (((pressureComp >> 3) * (pressureComp >> 3)) >> 13) * (parP9 >> 12);
    var2 =  (((pressureComp >> 2) * (parP8)) >> 13);
    var3 +=  ((pressureComp >> 8) * (pressureComp >> 8) * (pressureComp >> 8) * (parP10 >> 17));
    pressureComp += (var3 + var2 + (parP7 << 7)) >> 4;
    return pressureComp/100.0;
  }

}
