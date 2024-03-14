package io.mapsmessaging.devices.i2c.devices.sensors.bme688.measurement;

import io.mapsmessaging.devices.i2c.devices.sensors.bme688.BME688Sensor;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.register.ValueRegister;

import java.io.IOException;

public class HumidityMeasurement implements Measurement {

  private static final int[] HUMIDITY_ADDRESS = {0x25, 0x36, 0x47};

  private final ValueRegister humidityRegister;
  private final HumidityCalibrationData humidityCalibrationData;
  private final TemperatureCalibrationData temperatureCalibrationData;

  public HumidityMeasurement(BME688Sensor sensor,
                             int index,
                             HumidityCalibrationData humidityCalibrationData,
                             TemperatureCalibrationData temperatureCalibrationData) throws IOException {
    humidityRegister = new ValueRegister(sensor, HUMIDITY_ADDRESS[index], "hum_" + index);
    this.humidityCalibrationData = humidityCalibrationData;
    this.temperatureCalibrationData = temperatureCalibrationData;
  }

  @Override
  public double getMeasurement() throws IOException {
    humidityCalibrationData.load(); // Ensure calibration data is loaded
    long rawHumidity = humidityRegister.getValue();
    long tFine = temperatureCalibrationData.getTFine();

    int var1;
    int var2;
    int var3;
    int var4;
    int var5;
    int var6;
    int tempScaled;
    int calcHum;

    tempScaled = ((int) (tFine * 5 + 128) >> 8);
    var1 = (int) (rawHumidity - ((int) (humidityCalibrationData.getParH1() * 16))) -
        ((tempScaled * humidityCalibrationData.getParH3() / 100) >> 1);
    var2 = (humidityCalibrationData.getParH2()
        * ((tempScaled * humidityCalibrationData.getParH4() / 100)
        + ((tempScaled * ((tempScaled * humidityCalibrationData.getParH5() / 100)) >> 6) / 100)
        + (1 << 14))) >> 10;
    var3 = var1 * var2;
    var4 = humidityCalibrationData.getParH6() << 7;
    var4 = (var4 + (tempScaled * humidityCalibrationData.getParH7() / 100)) >> 4;
    var5 = ((var3 >> 14) * (var3 >> 14)) >> 10;
    var6 = (var4 * var5) >> 1;
    calcHum = (((var3 + var6) >> 10) * 1000) >> 12;

    if (calcHum > 100000) { // Cap at 100%rH
      calcHum = 100000;
    } else if (calcHum < 0) {
      calcHum = 0;
    }

    return calcHum / 1000.0; // Convert to percentage
  }

}
