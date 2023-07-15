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

package io.mapsmessaging.devices.i2c.devices.sensors.bmp280.values;

import lombok.Getter;

public enum OversamplingRate {

  D1_OSR_256((byte) 0x40),
  D1_OSR_512((byte) 0x42),
  D1_OSR_1024((byte) 0x44),
  D1_OSR_2048((byte) 0x46),
  D1_OSR_4096((byte) 0x48),
  D2_OSR_256((byte) 0x50),
  D2_OSR_512((byte) 0x52),
  D2_OSR_1024((byte) 0x54),
  D2_OSR_2048((byte) 0x56),
  D2_OSR_4096((byte) 0x58);

  @Getter
  private final byte value;

  OversamplingRate(byte value) {
    this.value = value;
  }
}
