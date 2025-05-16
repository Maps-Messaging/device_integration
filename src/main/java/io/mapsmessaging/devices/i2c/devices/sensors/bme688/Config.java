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

package io.mapsmessaging.devices.i2c.devices.sensors.bme688;

public class Config {
  public static final int PERIOD_POLL = 10_000;
  public static final byte CHIP_ID = 0x61;
  public static final int PERIOD_RESET = 10_000;
  public static final byte I2C_ADDR_LOW = 0x76;
  public static final byte I2C_ADDR_HIGH = 0x77;
  public static final byte SOFT_RESET_CMD = (byte) 0xb6;
  public static final byte OK = 0;
  public static final byte ERROR_NULL_PTR = -1;
  public static final byte ERROR_COMMUNICATION_FAIL = -2;
  public static final byte ERROR_DEVICE_NOT_FOUND = -3;
  public static final byte ERROR_INVALID_LENGTH = -4;
  public static final byte ERROR_SELF_TEST = -5;
  public static final byte WARNING_DEFINE_OP_MODE = 1;
  public static final byte WARNING_NO_NEW_DATA = 2;
  public static final byte WARNING_DEFINE_SHARED_HEATER_DURATION = 3;
  public static final byte INFO_PARAMETER_CORRECTION = 1;

  // Register addresses
  public static final int REG_CHIP_ID = 0xD0;
  public static final int REG_SOFT_RESET = 0xE0;
}


