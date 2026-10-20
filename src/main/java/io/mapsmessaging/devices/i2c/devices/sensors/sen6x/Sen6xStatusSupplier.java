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

package io.mapsmessaging.devices.i2c.devices.sensors.sen6x;

import io.mapsmessaging.devices.i2c.devices.sensors.sen6x.commands.GetDeviceStatusCommand;

public class Sen6xStatusSupplier {
  private final GetDeviceStatusCommand command;

  public Sen6xStatusSupplier(GetDeviceStatusCommand command) {
    this.command = command;
  }

  public boolean isFanError() throws java.io.IOException     { return command.execute().isFanError(); }
  public boolean isRhtError() throws java.io.IOException     { return command.execute().isRhtError(); }
  public boolean isGasError() throws java.io.IOException     { return command.execute().isGasError(); }
  public boolean isCo2_2Error() throws java.io.IOException   { return command.execute().isCo2_2Error(); }
  public boolean isHchoError() throws java.io.IOException    { return command.execute().isHchoError(); }
  public boolean isPmError() throws java.io.IOException      { return command.execute().isPmError(); }
  public boolean isCo2_1Error() throws java.io.IOException   { return command.execute().isCo2_1Error(); }
  public boolean isSpeedWarning() throws java.io.IOException { return command.execute().isSpeedWarning(); }
  public boolean isCompensationActive() throws java.io.IOException { return command.execute().isCompensationActive(); }

}
