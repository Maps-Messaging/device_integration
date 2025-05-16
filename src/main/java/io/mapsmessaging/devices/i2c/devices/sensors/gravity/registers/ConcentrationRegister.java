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

package io.mapsmessaging.devices.i2c.devices.sensors.gravity.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.gravity.config.Command;
import io.mapsmessaging.devices.i2c.devices.sensors.gravity.module.SensorType;

import java.io.IOException;

public class ConcentrationRegister extends CrcValidatingRegister {

  public ConcentrationRegister(I2CDevice sensor) {
    super(sensor, Command.GET_GAS_CONCENTRATION);
  }

  public float getConcentration() throws IOException {
    byte[] data = new byte[9];
    request(new byte[6], data);
    float concentration = (data[2] << 8 | (data[3] & 0xff));
    concentration = adjustPowers(data[5], concentration);
    return concentration;
  }

  public SensorType getSensorType() throws IOException {
    byte[] data = new byte[9];
    if (request(new byte[6], data)) {
      return SensorType.getByType(data[4]);
    }
    return SensorType.UNKNOWN;
  }

}
