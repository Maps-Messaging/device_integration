package io.mapsmessaging.devices.i2c.devices.sensors.bme688;

import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.register.GasReadingRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.register.LargeValueRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.register.MeasurementStatusRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.register.ValueRegister;

import java.io.IOException;


public class SensorReadings {
  private static final int[] MEASUREMENT_ADDRESSES = {0x1D, 0x2E, 0x3f};
  private static final int[] MEASURE_IDX_ADDRESSES = {0x1E, 0x2F, 0x40};
  private static final int[] PRESSURE_ADDRESSES    = {0x1F, 0x30, 0x41};
  private static final int[] TEMPERATURE_ADDRESS   = {0x22, 0x33, 0x44};
  private static final int[] HUMIDITY_ADDRESS      = {0x25, 0x36, 0x47};
  private static final int[] GAS_ADDRESSES         = {0x2C, 0x3D, 0x4E};

  private final LargeValueRegister pressureMeasurementRegister;
  private final ValueRegister humidityRegister;
  private final GasReadingRegister gasReadingRegister;
  private final SingleByteRegister subMeasureIndex;
  private final MeasurementStatusRegister measurementStatusRegister;

  private final LargeValueRegister temperatureMeasurementRegister;
  private final TemperatureCalibrationData temperatureCalibrationData;

  public SensorReadings(BME688Sensor sensor, int index, TemperatureCalibrationData temperatureCalibrationData) throws IOException {
    pressureMeasurementRegister = new LargeValueRegister(sensor, PRESSURE_ADDRESSES[index], "temp_"+index );
    temperatureMeasurementRegister = new LargeValueRegister(sensor, TEMPERATURE_ADDRESS[index], "temp_"+index );
    humidityRegister = new ValueRegister(sensor, HUMIDITY_ADDRESS[index], "hum_"+index);
    gasReadingRegister = new GasReadingRegister(sensor, GAS_ADDRESSES[index], "Gas_r_"+ index);
    subMeasureIndex = new SingleByteRegister(sensor, MEASURE_IDX_ADDRESSES[index], "sub_meas_index_"+index );
    measurementStatusRegister = new MeasurementStatusRegister(sensor, MEASUREMENT_ADDRESSES[index], "meas_status_"+index);
    this.temperatureCalibrationData = temperatureCalibrationData;
  }


  public boolean hasData() throws IOException {
    measurementStatusRegister.read();
    return measurementStatusRegister.hasNewData();
  }


  public double readCompensatedTemperature() throws IOException {
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
    return temperature;
  }
}
