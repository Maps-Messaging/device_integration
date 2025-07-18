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

package io.mapsmessaging.devices.i2c.devices.sensors.sen66.commands;

import io.mapsmessaging.devices.i2c.devices.sensors.sen66.Sen6xCommandHelper;

import java.io.IOException;

public class ClearDeviceStatusCommand implements Sen6xCommand<Boolean> {

  private static final int CMD_ID = 0xD210;
  private static final int DELAY_MS = 20;

  private final Sen6xCommandHelper helper;

  public ClearDeviceStatusCommand(Sen6xCommandHelper helper) {
    this.helper = helper;
  }

  @Override
  public Boolean execute() throws IOException {
    helper.sendCommand(CMD_ID);
    helper.delay(DELAY_MS);
    return true;
  }

  public boolean clear() {
    try {
      return execute();
    } catch (IOException e) {
      return false;
    }
  }
}
