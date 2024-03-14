package io.mapsmessaging.devices.i2c.devices.sensors.bme688;

import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.measurement.*;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.register.MeasurementStatusRegister;

import java.io.IOException;


public class SensorReadings {
  private static final int[] MEASUREMENT_ADDRESSES = {0x1D, 0x2E, 0x3f};
  private static final int[] MEASURE_IDX_ADDRESSES = {0x1E, 0x2F, 0x40};

  private final HumidityMeasurement humidityMeasurement;
  private final PressureMeasurement pressureMeasurement;
  private final TemperatureMeasurement temperatureMeasurement;
  private final GasMeasurement gasMeasurement;

  private final SingleByteRegister subMeasureIndex;
  private final MeasurementStatusRegister measurementStatusRegister;


  public SensorReadings(BME688Sensor sensor, int index,
                        TemperatureCalibrationData temperatureCalibrationData,
                        HumidityCalibrationData humidityCalibrationData,
                        PressureCalibrationData pressureCalibrationData
  ) throws IOException {

    humidityMeasurement = new HumidityMeasurement(sensor, index, humidityCalibrationData, temperatureCalibrationData);
    pressureMeasurement = new PressureMeasurement(sensor, index, pressureCalibrationData, temperatureCalibrationData);
    temperatureMeasurement = new TemperatureMeasurement(sensor, index, temperatureCalibrationData);
    gasMeasurement = new GasMeasurement(sensor, index);

    subMeasureIndex = new SingleByteRegister(sensor, MEASURE_IDX_ADDRESSES[index], "sub_meas_index_" + index);
    measurementStatusRegister = new MeasurementStatusRegister(sensor, MEASUREMENT_ADDRESSES[index], "meas_status_" + index);
  }


  public boolean hasData() throws IOException {
    measurementStatusRegister.read();
    return measurementStatusRegister.hasNewData();
  }

  public double getTemperature() throws IOException {
    return temperatureMeasurement.getMeasurement();
  }

  public double getHumidity() throws IOException {
    return humidityMeasurement.getMeasurement();
  }

  public double getPressure() throws IOException {
    return pressureMeasurement.getMeasurement();
  }

  public double getGas() throws IOException {
    return gasMeasurement.getMeasurement();
  }

}
