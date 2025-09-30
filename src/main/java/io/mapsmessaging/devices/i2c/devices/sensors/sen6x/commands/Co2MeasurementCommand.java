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

import io.mapsmessaging.devices.i2c.devices.sensors.sen6x.ResetMonitor;

import java.io.IOException;

public class Co2MeasurementCommand extends AbstractMeasurementCommand {

  private final ResetMonitor resetMonitor;

  public Co2MeasurementCommand(ResetMonitor resetMonitor, Sen6xMeasurementManager manager) {
    super(
        manager,
        "CO₂",                     // name
        "ppm",                    // unit
        "Carbon dioxide level",   // description
        420.0f,                   // example
        true,                     // readOnly
        400.0f,                   // min
        5000.0f,                  // max
        0                         // precision (integer ppm)
    );
    this.resetMonitor = resetMonitor;
  }

  @Override
  public float getValue() throws IOException {
    float val =manager.getMeasurementBlock().getCo2ppm();
    resetMonitor.check();
    return val;
  }

}
