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
import io.mapsmessaging.devices.i2c.devices.sensors.sen6x.data.MeasurementBlock;

import java.io.IOException;

public abstract class Sen6xMeasurementManager {

  private final Sen6xCommandHelper helper;
  private final int commandId;
  private final int length;
  private long lastReadTime = 0;
  private MeasurementBlock cachedBlock;
  private GetDataReadyFlagCommand getReadyFlagCommand;

  public Sen6xMeasurementManager(Sen6xCommandHelper helper, int commandId, int length) {
    this.helper = helper;
    this.length = length;
    this.commandId = commandId;
    getReadyFlagCommand = new GetDataReadyFlagCommand(helper);
    cachedBlock = new MeasurementBlock();
  }

  public synchronized MeasurementBlock getMeasurementBlock() throws IOException {
    if (getReadyFlagCommand.isReady()) {
      long now = System.currentTimeMillis();
      if (cachedBlock == null || now - lastReadTime > 1000) {
        byte[] raw = helper.requestResponse(commandId, length);
        cachedBlock = processResponse(raw);
        lastReadTime = now;
      }
    }
    return cachedBlock;
  }

  protected abstract MeasurementBlock processResponse(byte[] data) throws IOException;


  protected static int parseUInt16(byte[] raw, int offset) {
    return ((raw[offset] & 0xFF) << 8) | (raw[offset + 1] & 0xFF);
  }

  protected static short parseInt16(byte[] raw, int offset) {
    return (short) (((raw[offset] & 0xFF) << 8) | (raw[offset + 1] & 0xFF));
  }
}
