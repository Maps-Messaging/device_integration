/*
 *
 *  Copyright [ 2020 - 2024 ] [Matthew Buckton]
 *  Copyright [ 2024 - 2025.  ] [Maps Messaging B.V.]
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package io.mapsmessaging.devices.i2c.devices.sensors.gravity.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.sensors.gravity.config.Command;

import java.io.IOException;

public class VoltageRegister extends CrcValidatingRegister {

  public VoltageRegister(I2CDevice sensor) {
    super(sensor, Command.SENSOR_VOLTAGE);
  }

  public float getVoltage() throws IOException {
    byte[] recvbuf = new byte[9];
    if (request(new byte[6], recvbuf)) {
      return ((recvbuf[2] << 8 | recvbuf[3] & 0xff) * 3.0f) / 1024.0f * 2f;
    }
    return Float.NaN;
  }

}

