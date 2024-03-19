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

    int var1 = (rawTemp >> 3) - (parT1 << 1);
    int var2 = (var1 * parT2) >> 11;
    int var3 = ((((var1 >>1 ) * (var1 >>1))>>12) * ( parT3 <<4)) >> 14;
    int tFine = var2 + var3;
    temperatureCalibrationData.setTFine(tFine);
    return (((tFine * 5) + 128) >> 8) /100.0;
  }
}
