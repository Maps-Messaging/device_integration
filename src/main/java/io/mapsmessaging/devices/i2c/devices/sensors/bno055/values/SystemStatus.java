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

package io.mapsmessaging.devices.i2c.devices.sensors.bno055.values;

import lombok.Getter;

@Getter
public enum SystemStatus {
  IDLE("System idle"),
  ERROR("System Error"),
  INITIALIZING_PERIPHERALS("Initializing peripherals"),
  SYSTEM_INITIALIZATION("System Initialization"),
  EXECUTING_SELFTEST("Executing selftest"),
  SENSOR_FUSION_RUNNING("Sensor fusion algorithm running"),
  SYSTEM_RUNNING_WITHOUT_FUSION("System running without fusion algorithm");

  private final String description;

  SystemStatus(String description) {
    this.description = description;
  }
}
