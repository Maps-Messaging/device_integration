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

package io.mapsmessaging.devices.i2c.devices.sensors.as3935.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;

import java.io.IOException;

public class LightningStrikeRegister extends SingleByteRegister {
  private static final int LIGHTNING_STRIKE_LSB_ADDR = 0x04;

  public LightningStrikeRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0x04, "Lightning Strike");
  }

  public int getEnergy() throws IOException {
    byte[] energy = new byte[3];
    if (sensor.readRegister(LIGHTNING_STRIKE_LSB_ADDR, energy, 0, energy.length) != energy.length) {
      throw new IOException("Incomplete AS3935 energy reading");
    }
    return ((energy[2] & 0x1f) << 16) | ((energy[1] & 0xff) << 8) | (energy[0] & 0xff);
  }
}