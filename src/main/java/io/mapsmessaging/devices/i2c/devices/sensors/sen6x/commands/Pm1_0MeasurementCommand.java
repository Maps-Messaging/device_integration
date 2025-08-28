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

import java.io.IOException;

public class Pm1_0MeasurementCommand extends AbstractMeasurementCommand {

  public Pm1_0MeasurementCommand(Sen6xMeasurementManager manager) {
    super(
        manager,
        "pm1_0",
        "µg/m³",
        "Particulate Matter 1.0 concentration",
        5.0f,
        true,
        0.0f,
        1000.0f,
        1
    );
  }

  @Override
  public float getValue() throws IOException {
    return manager.getMeasurementBlock().getPm1_0();
  }
}
