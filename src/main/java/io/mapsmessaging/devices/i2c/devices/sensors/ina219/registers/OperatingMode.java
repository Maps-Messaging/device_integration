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

package io.mapsmessaging.devices.i2c.devices.sensors.ina219.registers;

import lombok.Getter;

@Getter
public enum OperatingMode {
  POWERDOWN(0x0000),
  SVOLT_TRIGGERED(0x0001),
  BVOLT_TRIGGERED(0x0002),
  SANDBVOLT_TRIGGERED(0x0003),
  ADCOFF(0x0004),
  SVOLT_CONTINUOUS(0x0005),
  BVOLT_CONTINUOUS(0x0006),
  SANDBVOLT_CONTINUOUS(0x0007);

  private final int value;

  OperatingMode(int value) {
    this.value = value;
  }

}