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
                             PressureCalibrationData pressureCalibrationData,
                             TemperatureCalibrationData temperatureCalibrationData
  ) {
    pressureMeasurementRegister = new LargeValueRegister(sensor, PRESSURE_ADDRESSES[index], "temp_" + index);
    this.pressureCalibrationData = pressureCalibrationData;
    this.temperatureCalibrationData = temperatureCalibrationData;
  }

  @Override
  public double getMeasurement() throws IOException {
    long presAdc = pressureMeasurementRegister.getValue(); // Ensure this handles the sign correctly if needed
    long parP1 = pressureCalibrationData.getParP1();
    long parP2 = pressureCalibrationData.getParP2();
    int parP3 = pressureCalibrationData.getParP3();
    long parP4 = pressureCalibrationData.getParP4();
    long parP5 = pressureCalibrationData.getParP5();
    int parP6 = pressureCalibrationData.getParP6();
    int parP7 = pressureCalibrationData.getParP7();
    long parP8 = pressureCalibrationData.getParP8();
    long parP9 = pressureCalibrationData.getParP9();
    int parP10 = pressureCalibrationData.getParP10();

    float var1;
    float var2;
    float var3;
    float calcPres;

    var1 = (((float) temperatureCalibrationData.getTFine() / 2.0f) - 64000.0f);
    var2 = var1 * var1 * ((parP6) / 131072.0f);
    var2 = var2 + (var1 * (parP5) * 2.0f);
    var2 = (var2 / 4.0f) + ((parP4) * 65536.0f);
    var1 = ((((parP3 * var1 * var1) / 16384.0f) + (parP2 * var1)) / 524288.0f);
    var1 = ((1.0f + (var1 / 32768.0f)) * (parP1));
    calcPres = (1048576.0f - (presAdc));

    // Avoid exception caused by division by zero
    if ((int) var1 != 0) {
      calcPres = (((calcPres - (var2 / 4096.0f)) * 6250.0f) / var1);
      var1 = ((parP9) * calcPres * calcPres) / 2147483648.0f;
      var2 = calcPres * ((parP8) / 32768.0f);
      var3 = ((calcPres / 256.0f) * (calcPres / 256.0f) * (calcPres / 256.0f) * (parP10 / 131072.0f));
      calcPres = (calcPres + (var1 + var2 + var3 + (parP7) * 128.0f)) / 16.0f;
    } else {
      calcPres = 0;
    }

    return calcPres;
  }


}
