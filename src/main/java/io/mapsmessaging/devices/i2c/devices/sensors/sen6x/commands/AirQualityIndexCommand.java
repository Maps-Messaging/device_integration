/*
 *    Copyright [ 2020 - 2024 ] Matthew Buckton
 *    Copyright [ 2024 - 2025 ] MapsMessaging B.V.
 *
 *    Licensed under the Apache License, Version 2.0 with the Commons Clause
 *    (the "License"); you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at:
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *        https://commonsclause.com/
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License
 */

package io.mapsmessaging.devices.i2c.devices.sensors.sen6x.commands;


import io.mapsmessaging.devices.i2c.devices.sensors.utils.AqiCalculator;

import java.io.IOException;

public class AirQualityIndexCommand extends AbstractMeasurementCommand {

  public AirQualityIndexCommand(Sen6xMeasurementManager manager) {
    super(
        manager,
        "airQualityIndex",
        "AQI",
        "Computed air quality index based on sensor data",
        75.0f,
        true,
        0.0f,
        500.0f,
        0
    );
  }

  @Override
  public float getValue() throws IOException {
    var block = manager.getMeasurementBlock();
    return AqiCalculator.computeFromSEN66(
        block.getPm2_5(),
        block.getVocIndex(),
        block.getNoxIndex()
    );
  }
}
