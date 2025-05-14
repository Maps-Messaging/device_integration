/*
 *
 *  Copyright [ 2020 - 2024 ] [Matthew Buckton]
 *  Copyright [ 2024 - 2025.  ] [Maps Messaging B.V.]
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package io.mapsmessaging.devices.i2c.devices.sensors.bme688.measurement;

import io.mapsmessaging.devices.i2c.devices.sensors.bme688.BME688Sensor;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.register.GasReadingRegister;

import java.io.IOException;

public class GasMeasurement implements Measurement {
  private static final int[] GAS_ADDRESSES = {0x2C, 0x3D, 0x4E};

  private final GasReadingRegister gasReadingRegister;

  public GasMeasurement(BME688Sensor sensor, int index, CalibrationData calibrationData) {
    gasReadingRegister = new GasReadingRegister(sensor, GAS_ADDRESSES[index], "Gas_r_" + index);
  }

  @Override
  public double getMeasurement() throws IOException {
    if (gasReadingRegister.isGasValid()) {
      long gasResAdc = gasReadingRegister.getGasReading();
      int gasRange = gasReadingRegister.getGasRange();

      long var1 = 0x40000 >> gasRange;
      double dVar2 = 0x1000 + ((gasResAdc - 0x200) * 3f);
      return 1000000.0f * var1 / dVar2;
    }
    return Double.NaN;
  }

}
