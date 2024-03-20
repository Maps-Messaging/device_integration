package io.mapsmessaging.devices.i2c.devices.sensors.bme688;

import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.measurement.*;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.register.MeasurementStatusRegister;
import lombok.Getter;
import lombok.Setter;

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

  private long lastRead;

  @Getter
  private double temperature;
  @Getter
  private double pressure;
  @Getter
  private double humidity;
  @Getter
  private double gas;

  @Getter
  @Setter
  private long dataReady;

  public SensorReadings(BME688Sensor sensor, int index,
                        TemperatureCalibrationData temperatureCalibrationData,
                        HumidityCalibrationData humidityCalibrationData,
                        PressureCalibrationData pressureCalibrationData,
                        GasCalibrationData gasCalibrationData
  ) throws IOException {
    humidityMeasurement = new HumidityMeasurement(sensor, index, humidityCalibrationData, temperatureCalibrationData);
    pressureMeasurement = new PressureMeasurement(sensor, index, pressureCalibrationData, temperatureCalibrationData);
    temperatureMeasurement = new TemperatureMeasurement(sensor, index, temperatureCalibrationData);
    gasMeasurement = new GasMeasurement(sensor, index, gasCalibrationData, temperatureCalibrationData);

    subMeasureIndex = new SingleByteRegister(sensor, MEASURE_IDX_ADDRESSES[index], "sub_meas_index_" + index);
    measurementStatusRegister = new MeasurementStatusRegister(sensor, MEASUREMENT_ADDRESSES[index], "meas_status_" + index);
    lastRead = subMeasureIndex.getRegisterValue();
  }

  protected boolean hasData() throws IOException {
    measurementStatusRegister.read();
    subMeasureIndex.read();
    return measurementStatusRegister.hasNewData() &&   // new data not yet read
        !measurementStatusRegister.isReadingGas() &&   // Not performing a gas reading
        !measurementStatusRegister.isMeasuring() &&    // not performing a temp/humidity/ pressure
        subMeasureIndex.getRegisterValue() != lastRead;// device has incremented the internal counter
  }

  public void doMeasurements() throws IOException {
    hasData();
    if(!measurementStatusRegister.isReadingGas()) {
      lastRead = subMeasureIndex.getRegisterValue();
      temperature = temperatureMeasurement.getMeasurement(); // Must be performed first to compute t_fine
      humidity = humidityMeasurement.getMeasurement();
      pressure = pressureMeasurement.getMeasurement();
      gas = gasMeasurement.getMeasurement();
    }
  }

}
