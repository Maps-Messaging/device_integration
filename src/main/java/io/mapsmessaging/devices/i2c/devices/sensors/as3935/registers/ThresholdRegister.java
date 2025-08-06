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

package io.mapsmessaging.devices.i2c.devices.sensors.as3935.registers;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.as3935.data.ThresholdData;

import java.io.IOException;

public class ThresholdRegister extends SingleByteRegister {

  private static final int THRESHOLD_NF_LEV_BITS = 4;

  public ThresholdRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0x1, "Threshold");
    reload();
  }

  // THRESHOLD Register : 1
  public int getWatchdogThreshold() {
    return registerValue & 0x0F;
  }


  public void setWatchdogThreshold(int threshold) throws IOException {
    registerValue &= ~(0x0F);
    registerValue |= (threshold & 0x0F);
    sensor.write(address, registerValue);
  }

  public int getNoiseFloorLevel() {
    return (registerValue >> THRESHOLD_NF_LEV_BITS) & 0x07;
  }

  public void setNoiseFloorLevel(int level) throws IOException {
    registerValue &= ~((0x07) << THRESHOLD_NF_LEV_BITS);
    registerValue |= (level << THRESHOLD_NF_LEV_BITS) & ((0x07) << THRESHOLD_NF_LEV_BITS);
    sensor.write(address, registerValue);
  }

  @Override
  public RegisterData toData() throws IOException {
    int watchdogThreshold = getWatchdogThreshold();
    int noiseFloorLevel = getNoiseFloorLevel();
    return new ThresholdData(watchdogThreshold, noiseFloorLevel);
  }

  @Override
  public boolean fromData(RegisterData input) throws IOException {
    if (input instanceof ThresholdData data) {
      setWatchdogThreshold(data.getWatchdogThreshold());
      setNoiseFloorLevel(data.getNoiseFloorLevel());
      return true;
    }
    return false;
  }
}
