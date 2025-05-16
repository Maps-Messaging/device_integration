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

package io.mapsmessaging.devices.i2c.devices.sensors.tsl2561.registers;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.MultiByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.tsl2561.data.LowThresholdData;

import java.io.IOException;

public class LowThresholdRegister extends MultiByteRegister {

  public LowThresholdRegister(I2CDevice sensor) {
    super(sensor, 0x82, 2, "LowThresholdRegister");
  }

  public RegisterData toData() throws IOException {
    return new LowThresholdData(asInt());
  }

  public boolean fromData(RegisterData input) throws IOException {
    if (input instanceof LowThresholdData) {
      LowThresholdData data = (LowThresholdData) input;
      super.write(data.getThreshold());
      return true;
    }
    return false;
  }
}
