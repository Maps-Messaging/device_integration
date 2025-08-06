/*
 *
 *  Copyright [ 2020 - 2024 ] Matthew Buckton
 *  Copyright [ 2024 - 2025 ] MapsMessaging B.V.
 *
 *  Licensed under the Apache License, Version 2.0 with the Commons Clause
 *  (the "License"); you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      https://commonsclause.com/
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */

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

  public SensorReadings(BME688Sensor sensor, int index, CalibrationData calibrationData) throws IOException {
    humidityMeasurement = new HumidityMeasurement(sensor, index, calibrationData);
    pressureMeasurement = new PressureMeasurement(sensor, index, calibrationData);
    temperatureMeasurement = new TemperatureMeasurement(sensor, index, calibrationData);
    gasMeasurement = new GasMeasurement(sensor, index);

    subMeasureIndex = new SingleByteRegister(sensor, MEASURE_IDX_ADDRESSES[index], "sub_meas_index_" + index);
    measurementStatusRegister = new MeasurementStatusRegister(sensor, MEASUREMENT_ADDRESSES[index], "meas_status_" + index);
    lastRead = subMeasureIndex.getRegisterValue();
  }

  public void doMeasurements() throws IOException {
    measurementStatusRegister.read();
    subMeasureIndex.read();
    if (!measurementStatusRegister.isReadingGas() && !measurementStatusRegister.isMeasuring()) {
      lastRead = subMeasureIndex.getRegisterValue();
      temperature = temperatureMeasurement.getMeasurement(); // Must be performed first to compute t_fine
      humidity = humidityMeasurement.getMeasurement();
      pressure = pressureMeasurement.getMeasurement();
      gas = gasMeasurement.getMeasurement();
    }
  }

}
