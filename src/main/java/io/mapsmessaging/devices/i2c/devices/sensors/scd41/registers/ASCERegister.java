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

package io.mapsmessaging.devices.i2c.devices.sensors.scd41.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions.GetASCERequest;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions.SetASCERequest;

public class ASCERegister extends RequestRegister {

  public ASCERegister(I2CDevice sensor) {
    // Since the initial state requires just checking, we'll associate it with getting the ASCE state.
    super(sensor, "ASCE", new GetASCERequest(sensor.getDevice()));
  }

  public boolean isASCEEnabled() {
    // Ensure the request is of the correct type and execute its specific method.
    if (request instanceof GetASCERequest) {
      return ((GetASCERequest) request).isASCEEnabled();
    }
    // In case the request instance is not of expected type, consider ASCE disabled or handle appropriately.
    return false;
  }

  public void setASCEState(boolean flag) {
    // Create a new SetASCERequest instance to change the ASCE state.
    SetASCERequest setRequest = new SetASCERequest(this.request.getDevice());
    setRequest.setASCEState(flag);
    // Optionally, update the 'request' member to reflect the new state if required for subsequent operations.
  }
}
