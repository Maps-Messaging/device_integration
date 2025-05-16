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

package io.mapsmessaging.devices.i2c.devices.output.led.ht16k33;

import lombok.Getter;

@Getter
public enum SevenSegmentLed {
  TOP(0b00000001),
  TOP_RIGHT(0b00000010),
  BOTTOM_RIGHT(0b00000100),
  BOTTOM(0b00001000),
  BOTTOM_LEFT(0b00010000),
  TOP_LEFT(0b00100000),
  MIDDLE(0b01000000),
  DECIMAL(0b10000000),
  COLON(0b11111111);

  private final int mask;


  SevenSegmentLed(int mask) {
    this.mask = mask;
  }
}
