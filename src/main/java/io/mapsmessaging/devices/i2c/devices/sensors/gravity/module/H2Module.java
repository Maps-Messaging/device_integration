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

package io.mapsmessaging.devices.i2c.devices.sensors.gravity.module;

public class H2Module extends SensorModule {

  public H2Module() {
    super();
  }

  @Override
  protected float calculateSensorConcentration(float temperature, float rawConcentration) {
    if (temperature > -20 && temperature <= 20) {
      return rawConcentration / (0.0074f * temperature + 0.7f) - 5;
    }
    if (temperature > 20 && temperature <= 40) {
      return rawConcentration / (0.025f * temperature + 0.3f) - 5;
    }
    if (temperature > 40 && temperature <= 60) {
      return (rawConcentration / (0.001f * temperature + 0.9f)) - (0.75f * temperature - 25f);
    }
    return 0;
  }
}