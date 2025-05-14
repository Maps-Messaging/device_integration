/*
 *
 *  Copyright [ 2020 - 2024 ] [Matthew Buckton]
 *  Copyright [ 2024 - 2025.  ] [Maps Messaging B.V.]
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package io.mapsmessaging.devices.i2c.devices.output.led.ht16k33;

import lombok.Getter;

@Getter
public enum AlphaNumericLed {
  TOP(0b0000000000000001),
  TOP_RIGHT(0b0000000000000010),
  BOTTOM_RIGHT(0b0000000000000100),
  BOTTOM(0b0000000000001000),
  BOTTOM_LEFT(0b0000000000010000),
  TOP_LEFT(0b0000000000100000),
  CENTER_LEFT(0b0000000001000000),
  CENTER_RIGHT(0b0000000010000000),
  TOP_LEFT_DIAGONAL(0b0000000100000000),
  TOP_CENTER(0b0000001000000000),
  TOP_RIGHT_DIAGONAL(0b0000010000000000),
  BOTTOM_LEFT_DIAGONAL(0b0000100000000000),
  BOTTOM_CENTER(0b0001000000000000),
  BOTTOM_RIGHT_DIAGONAL(0b0010000000000000),
  DECIMAL(0b0100000000000000);

  private final int mask;

  AlphaNumericLed(int mask) {
    this.mask = mask;
  }

}
