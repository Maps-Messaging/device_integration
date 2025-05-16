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

package io.mapsmessaging.devices.i2c.devices.sensors.msa311.values;

import lombok.Getter;

@Getter
public enum Range {
  RANGE_2G(62.5, 4096f, 3.9f),
  RANGE_4G(125, 2048f, 7.8f),
  RANGE_8G(250, 1024f, 15.6f),
  RANGE_16G(500, 512f, 31.3f);

  private final double lsbMultiplier;

  @Getter
  private final float thresholdMultiplier;

  @Getter
  private final float scale;

  Range(double lsbMultiplier, float scale, float threshold) {
    this.lsbMultiplier = lsbMultiplier;
    this.scale = scale;
    this.thresholdMultiplier = threshold;
  }
}
