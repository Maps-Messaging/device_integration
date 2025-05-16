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
public enum OrientationStatus {
  Z_UP_PORTRAIT_UPRIGHT((byte) 0b000),
  Z_UP_PORTRAIT_UPSIDE_DOWN((byte) 0b001),
  Z_UP_LANDSCAPE_LEFT((byte) 0b010),
  Z_UP_LANDSCAPE_RIGHT((byte) 0b011),
  Z_DOWN_PORTRAIT_UPRIGHT((byte) 0b100),
  Z_DOWN_PORTRAIT_UPSIDE_DOWN((byte) 0b101),
  Z_DOWN_LANDSCAPE_LEFT((byte) 0b110),
  Z_DOWN_LANDSCAPE_RIGHT((byte) 0b111);

  private final byte mask;

  OrientationStatus(byte mask) {
    this.mask = mask;
  }
}
