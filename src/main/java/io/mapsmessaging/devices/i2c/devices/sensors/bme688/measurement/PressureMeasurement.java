/*
 *
 *  Copyright [ 2020 - 2024 ] Matthew Buckton
 *  Copyright [ 2024 - 2026 ] MapsMessaging B.V.
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

    // Bosch BME68x integer pressure compensation, in Pa.
    int var1 = (tFine >> 1) - 64000;
    int var2 = ((((var1 >> 2) * (var1 >> 2)) >> 11) * parP6) >> 2;
    var2 += (var1 * parP5) << 1;
    var2 = (var2 >> 2) + (parP4 << 16);
    int temperatureDelta = var1;
    var1 = (((((temperatureDelta >> 2) * (temperatureDelta >> 2)) >> 13) * (parP3 << 5)) >> 3)
        + ((parP2 * temperatureDelta) >> 1);
    var1 >>= 18;
    var1 = ((32768 + var1) * parP1) >> 15;
    if (var1 == 0) {
      throw new IOException("BME688 pressure compensation divisor is zero");
    }
    int pressureComp = (1048576 - presAdc - (var2 >> 12)) * 3125;
    if (pressureComp >= 0x40000000) {
      pressureComp = (pressureComp / var1) << 1;
    } else {
      pressureComp = (pressureComp << 1) / var1;
    }
    var1 = (parP9 * (((pressureComp >> 3) * (pressureComp >> 3)) >> 13)) >> 12;
    var2 = ((pressureComp >> 2) * parP8) >> 13;
    int var3 = ((pressureComp >> 8) * (pressureComp >> 8) * (pressureComp >> 8) * parP10) >> 17;
    pressureComp += (var1 + var2 + var3 + (parP7 << 7)) >> 4;
    return pressureComp / 100.0;
  }

}
