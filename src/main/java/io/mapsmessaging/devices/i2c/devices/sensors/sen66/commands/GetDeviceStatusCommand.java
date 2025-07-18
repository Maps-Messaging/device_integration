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
import io.mapsmessaging.devices.i2c.devices.sensors.sen66.data.Sen6xStatus;

import java.io.IOException;

public class GetDeviceStatusCommand implements Sen6xCommand<Sen6xStatus> {

  private static final int CMD_ID = 0xD206;
  private static final int DELAY_MS = 20;
  private static final int RESPONSE_LENGTH = 3; // 2 bytes + 1 CRC

  private final Sen6xCommandHelper helper;
  private long nextQuery;
  private Sen6xStatus sen6xstatus;

  public GetDeviceStatusCommand(Sen6xCommandHelper helper) {
    this.helper = helper;
  }

  @Override
  public Sen6xStatus execute() throws IOException {
    if (nextQuery < System.currentTimeMillis()) {
      byte[] data = helper.requestResponse(CMD_ID, RESPONSE_LENGTH, DELAY_MS);
      int status = ((data[0] & 0xFF) << 8) | (data[1] & 0xFF);
      sen6xstatus = new Sen6xStatus(status);
    }
    return sen6xstatus;
  }

  public Sen6xStatus get() {
    try {
      nextQuery = System.currentTimeMillis()+1000;
      return execute();
    } catch (IOException e) {
      return new Sen6xStatus(0); // or optionally throw / log
    }
  }
}
