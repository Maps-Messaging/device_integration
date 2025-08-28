/*
 *    Copyright [ 2020 - 2024 ] Matthew Buckton
 *    Copyright [ 2024 - 2025 ] MapsMessaging B.V.
 *
 *    Licensed under the Apache License, Version 2.0 with the Commons Clause
 *    (the "License"); you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at:
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *        https://commonsclause.com/
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License
 */

package io.mapsmessaging.devices.i2c.devices.sensors.sen6x.commands;

import io.mapsmessaging.devices.i2c.devices.sensors.sen6x.Sen6xCommandHelper;

import java.io.IOException;

public class GetFanCleaningIntervalCommand implements Sen6xCommand<Integer> {
  private static final int CMD_ID = 0xD210;
  private static final int DELAY_MS = 20;
  private static final int RESPONSE_LENGTH = 3;

  private final Sen6xCommandHelper helper;

  public GetFanCleaningIntervalCommand(Sen6xCommandHelper helper) {
    this.helper = helper;
  }

  @Override
  public Integer execute() throws IOException {
    byte[] data = helper.requestResponse(CMD_ID, RESPONSE_LENGTH, DELAY_MS);
    return ((data[0] & 0xFF) << 8) | (data[1] & 0xFF);
  }


  public int get(){
    try {
      return execute();
    } catch (IOException e) {
      return 0;
    }
  }
}
