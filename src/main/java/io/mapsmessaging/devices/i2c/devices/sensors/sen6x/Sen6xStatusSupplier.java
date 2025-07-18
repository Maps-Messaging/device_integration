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

package io.mapsmessaging.devices.i2c.devices.sensors.sen6x;

import io.mapsmessaging.devices.i2c.devices.sensors.sen6x.commands.GetDeviceStatusCommand;

public class Sen6xStatusSupplier {
  private final GetDeviceStatusCommand command;

  public Sen6xStatusSupplier(GetDeviceStatusCommand command) {
    this.command = command;
  }

  public boolean isFanError()     { return command.get().isFanError(); }
  public boolean isRhtError()     { return command.get().isRhtError(); }
  public boolean isGasError()     { return command.get().isGasError(); }
  public boolean isCo2_2Error()   { return command.get().isCo2_2Error(); }
  public boolean isHchoError()    { return command.get().isHchoError(); }
  public boolean isPmError()      { return command.get().isPmError(); }
  public boolean isCo2_1Error()   { return command.get().isCo2_1Error(); }
  public boolean isSpeedWarning() { return command.get().isSpeedWarning(); }
  public boolean isCompensationActive() { return command.get().isCompensationActive(); }

}
