/*
 *      Copyright [ 2020 - 2023 ] [Matthew Buckton]
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package io.mapsmessaging.devices.i2c.devices.sensors.as3935.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;

import java.io.IOException;

public class LightningStrikeRegister extends SingleByteRegister {
  private static final int LIGHTNING_STRIKE_MSB_ADDR = 0x04;
  private static final int LIGHTNING_STRIKE_LSB_ADDR = 0x05;
  private static final int LIGHTNING_STRIKE_BITS_0_TO_4_ADDR = 0x06;

  public LightningStrikeRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0x04, "Lightning Strike");
  }

  public int getEnergy() throws IOException {
    int msb = sensor.readRegister(LIGHTNING_STRIKE_MSB_ADDR);
    int lsb = sensor.readRegister(LIGHTNING_STRIKE_LSB_ADDR);
    int bits0to4 = sensor.readRegister(LIGHTNING_STRIKE_BITS_0_TO_4_ADDR);
    return ((bits0to4 & 0x1F) << 16) | (msb << 8) | lsb;
  }
}