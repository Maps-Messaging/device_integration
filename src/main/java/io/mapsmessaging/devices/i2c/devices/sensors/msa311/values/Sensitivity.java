/*
 *      Copyright [ 2020 - 2023 ] [Matthew Buckton]
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package io.mapsmessaging.devices.i2c.devices.sensors.msa311.values;

public enum Sensitivity {
  RANGE_2G(3.91),     // 3.91mg/LSB
  RANGE_4G(7.81),     // 7.81mg/LSB
  RANGE_8G(15.625),   // 15.625mg/LSB
  RANGE_16G(31.25);   // 31.25mg/LSB

  private final double factor;

  Sensitivity(double factor) {
    this.factor = factor;
  }

  public double getFactor() {
    return factor;
  }
}
