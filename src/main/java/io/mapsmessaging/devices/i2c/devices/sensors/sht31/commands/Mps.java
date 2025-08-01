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

package io.mapsmessaging.devices.i2c.devices.sensors.sht31.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Mps {
  MPS_0_5(0x20, new int[]{0x32, 0x24, 0x2F}),
  MPS_1(0x21, new int[]{0x30, 0x26, 0x2D}),
  MPS_2(0x22, new int[]{0x36, 0x20, 0x2B}),
  MPS_4(0x23, new int[]{0x34, 0x22, 0x29}),
  MPS_10(0x27, new int[]{0x37, 0x21, 0x2A});

  private final int msb;
  private final int[] lsbByRepeatability;

  public int getLsb(Repeatability r) {
    return lsbByRepeatability[r.ordinal()];
  }
}
