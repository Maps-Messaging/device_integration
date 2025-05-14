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

package io.mapsmessaging.devices.i2c.devices.sensors.gravity.module;

public class HCLModule extends SensorModule {

  public HCLModule() {
    super();
  }

  @Override
  protected float calculateSensorConcentration(float temperature, float rawConcentration) {
    if (temperature > -20 && temperature <= 0) {
      return rawConcentration - (-0.0075f * temperature - 0.1f);
    }
    if (temperature > 0 && temperature <= 20) {
      return rawConcentration - (-0.1f);
    }
    if (temperature > 20 && temperature <= 50) {
      return rawConcentration - (-0.01f * temperature + 0.1f);
    }
    return rawConcentration;
  }
}