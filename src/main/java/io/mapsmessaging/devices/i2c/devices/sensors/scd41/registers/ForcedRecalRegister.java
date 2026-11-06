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

package io.mapsmessaging.devices.i2c.devices.sensors.scd41.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions.ForcedRecalibrationRequest;

public class ForcedRecalRegister extends RequestRegister {

  public ForcedRecalRegister(I2CDevice sensor) {
    super(sensor, "Force Recal", new ForcedRecalibrationRequest(sensor.getDevice()));
  }

  /**
   * Perform forced recalibration after stopping periodic measurement.
   *
   * @param referencePpm independently measured CO2 concentration in ppm
   * @return the correction applied by the sensor, in ppm
   */
  public int forceRecallibration(int referencePpm) {
    return ((ForcedRecalibrationRequest) request).recalibrate(referencePpm);
  }

  @Deprecated
  public void forceRecallibration() {
    throw new UnsupportedOperationException("SCD41 forced recalibration requires an explicit CO2 reference in ppm");
  }

}
