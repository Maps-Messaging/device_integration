package io.mapsmessaging.devices.i2c.devices.sensors.bme688.measurement;

import io.mapsmessaging.devices.i2c.devices.sensors.bme688.BME688Sensor;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.register.LargeValueRegister;

import java.io.IOException;

public class TemperatureMeasurement implements Measurement {
  private static final int[] TEMPERATURE_ADDRESS = {0x22, 0x33, 0x44};

  private final LargeValueRegister temperatureMeasurementRegister;
  private final TemperatureCalibrationData temperatureCalibrationData;

  public TemperatureMeasurement(BME688Sensor sensor, int index, TemperatureCalibrationData temperatureCalibrationData) throws IOException {
    temperatureMeasurementRegister = new LargeValueRegister(sensor, TEMPERATURE_ADDRESS[index], "temp_" + index);
    this.temperatureCalibrationData = temperatureCalibrationData;
  }

  public double getMeasurement() throws IOException {
    int rawTemp = temperatureMeasurementRegister.getValue();
    int parT1 = temperatureCalibrationData.getParT1();
    int parT2 = temperatureCalibrationData.getParT2();
    int parT3 = temperatureCalibrationData.getParT3();

    // Compensation formula as per the sensor specifications
    double var1;
    double var2;
    double temperature;
    var1 = (rawTemp / 16384.0 - parT1 / 1024.0) * parT2;
    var2 = (rawTemp / 131072.0 - parT1 / 8192.0) * (rawTemp / 131072.0 - parT1 / 8192.0) * parT3 * 16.0;
    temperature = (var1 + var2) / 5120.0;
    calculateTfine(rawTemp);
    return temperature;
  }

  public void calculateTfine(int rawTemperature) {
    long parT1 = temperatureCalibrationData.getParT1();
    long parT2 = temperatureCalibrationData.getParT2();
    long parT3 = temperatureCalibrationData.getParT3();

    long var1;
    long var2;
    var1 = ((((rawTemperature >> 3) - (parT1 << 1))) * parT2) >> 11;
    var2 = (((((rawTemperature >> 4) - parT1) * ((rawTemperature >> 4) - parT1)) >> 12) * parT3) >> 14;
    temperatureCalibrationData.setTFine(var1 + var2);
  }

}
