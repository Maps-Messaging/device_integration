/*
 *
 *  Copyright [ 2020 - 2024 ] Matthew Buckton
 *  Copyright [ 2024 - 2026 ] MapsMessaging B.V.
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

package io.mapsmessaging.devices.i2c.devices.sensors.sen6x.commands;

import io.mapsmessaging.devices.i2c.devices.sensors.sen6x.Sen6xCommandHelper;
import java.io.IOException;

/** SEN6x provides manual fan cleaning, not an automatic cleaning interval command. */
@Deprecated
public class GetFanCleaningIntervalCommand implements Sen6xCommand<Integer> {

  @SuppressWarnings("java:S1172") // Preserve the existing public constructor signature.
  public GetFanCleaningIntervalCommand(Sen6xCommandHelper helper) {
    // No bus operation: the SEN6x datasheet defines no such command.
  }

  @Override
  public Integer execute() throws IOException {
    throw unsupported();
  }

  public int get() {
    throw unsupported();
  }

  private UnsupportedOperationException unsupported() {
    return new UnsupportedOperationException("SEN6x does not support a fan-cleaning interval");
  }
}
