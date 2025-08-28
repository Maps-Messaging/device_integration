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
                             CalibrationData calibrationData) {
    humidityRegister = new ValueRegister(sensor, HUMIDITY_ADDRESS[index], "hum_" + index);
    this.humidityCalibrationData = calibrationData.getHumidityCalibrationData();
    this.temperatureCalibrationData = calibrationData.getTemperatureCalibrationData();
  }

  @Override
  public double getMeasurement() throws IOException {
    int humAdc = humidityRegister.getValue();
    int tFine = temperatureCalibrationData.getTFine();
    int parH1 = humidityCalibrationData.getParH1();
    int parH2 = humidityCalibrationData.getParH2();
    int parH3 = humidityCalibrationData.getParH3();
    int parH4 = humidityCalibrationData.getParH4();
    int parH5 = humidityCalibrationData.getParH5();
    int parH6 = humidityCalibrationData.getParH6();
    int parH7 = humidityCalibrationData.getParH7();


    int tempScaled = ((tFine * 5) + 128) >> 8;
    int var1 = (humAdc - (parH1 * 16)) - (((tempScaled * parH3) / (100)) >> 1);
    int var2 =
        (parH2 *
            (((tempScaled * parH4) / (100)) +
                (((tempScaled * ((tempScaled * parH5) / (100))) >> 6) / (100)) +
                (1 << 14))) >> 10;
    int var3 = var1 * var2;
    int var4 = parH6 << 7;
    var4 = ((var4) + ((tempScaled * parH7) / (100))) >> 4;
    int var5 = ((var3 >> 14) * (var3 >> 14)) >> 10;
    int var6 = (var4 * var5) >> 1;
    int calcHum = (((var3 + var6) >> 10) * (1000)) >> 12;
    if (calcHum > 100000) /* Cap at 100%rH */ {
      calcHum = 100000;
    } else if (calcHum < 0) {
      calcHum = 0;
    }

    return calcHum / 1000f;
  }

}
