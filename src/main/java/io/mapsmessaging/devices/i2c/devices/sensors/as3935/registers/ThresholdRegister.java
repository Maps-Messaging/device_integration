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
import io.mapsmessaging.devices.i2c.devices.Register;

import java.io.IOException;

public class ThresholdRegister extends Register {

  private static final int THRESHOLD_NF_LEV_BITS = 4;

  public ThresholdRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0x1);
    reload();
  }

  // THRESHOLD Register : 1
  public int getWatchdogThreshold() throws IOException {
    return registerValue & 0x0F;
  }


  public void setWatchdogThreshold(int threshold) throws IOException {
    registerValue &= ~(0x0F);
    registerValue |= (threshold) & ((0x0F));
    sensor.write(address, registerValue);
  }

  public int getNoiseFloorLevel() throws IOException {
    return (registerValue >> THRESHOLD_NF_LEV_BITS) & 0x07;
  }

  public void setNoiseFloorLevel(int level) throws IOException {
    registerValue &= ~((0x07) << THRESHOLD_NF_LEV_BITS);
    registerValue |= (level << THRESHOLD_NF_LEV_BITS) & ((0x07) << THRESHOLD_NF_LEV_BITS);
    sensor.write(address, registerValue);
  }
}
