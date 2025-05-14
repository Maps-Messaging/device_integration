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
public enum SystemErrorStatus {
  NO_ERROR("No error"),
  PERIPHERAL_INIT_ERROR("Peripheral initialization error"),
  SYSTEM_INIT_ERROR("System initialization error"),
  SELF_TEST_FAILED("Self test result failed"),
  REGISTER_VALUE_OUT_OF_RANGE("Register map value out of range"),
  REGISTER_ADDRESS_OUT_OF_RANGE("Register map address out of range"),
  REGISTER_WRITE_ERROR("Register map write error"),
  LOW_POWER_MODE_UNAVAILABLE("BNO low power mode not available for selected operation mode"),
  ACCELEROMETER_POWER_MODE_UNAVAILABLE("Accelerometer power mode not available"),
  FUSION_CONFIG_ERROR("Fusion algorithm configuration error"),
  SENSOR_CONFIG_ERROR("Sensor configuration error"),
  UNKNOWN_ERROR("Unknown error");

  private final String description;

  SystemErrorStatus(String description) {
    this.description = description;
  }
}
