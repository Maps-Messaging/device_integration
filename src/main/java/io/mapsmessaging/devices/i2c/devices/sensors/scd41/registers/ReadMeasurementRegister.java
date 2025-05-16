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

package io.mapsmessaging.devices.i2c.devices.sensors.scd41.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions.ReadMeasurementRequest;

public class ReadMeasurementRegister extends RequestRegister {

  private long lastRead;

  public ReadMeasurementRegister(I2CDevice sensor) {
    super(sensor, "get measurement", new ReadMeasurementRequest(sensor.getDevice()));
    lastRead = 0;
  }

  public boolean hasData() {
    if (lastRead < System.currentTimeMillis()) {
      request.getResponse();
      lastRead = System.currentTimeMillis() + 5000;
      return true;
    }
    return false;
  }

  public int getCo2() {
    hasData();
    return ((ReadMeasurementRequest) request).getCo2();
  }

  public float getHumidity() {
    hasData();
    return ((ReadMeasurementRequest) request).getHumidity();
  }

  public float getTemperature() {
    hasData();
    return ((ReadMeasurementRequest) request).getTemperature();
  }

}
