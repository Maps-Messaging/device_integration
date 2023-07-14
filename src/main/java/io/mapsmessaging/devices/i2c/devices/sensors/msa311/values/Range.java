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

public enum Range {
  RANGE_2G(62.5),
  RANGE_4G(125),
  RANGE_8G(250),
  RANGE_16G(500);

  private final double lsbMultiplier;

  Range(double lsbMultiplier) {
    this.lsbMultiplier = lsbMultiplier;
  }

  public double getLsbMultiplier() {
    return lsbMultiplier;
  }
}

