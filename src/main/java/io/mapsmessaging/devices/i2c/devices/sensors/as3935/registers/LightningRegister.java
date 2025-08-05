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
import io.mapsmessaging.devices.i2c.devices.sensors.as3935.data.LightningData;

import java.io.IOException;

public class LightningRegister extends SingleByteRegister {

  private static final int LIGHTNING_REG_SREJ_BITS = 0;
  private static final int LIGHTNING_REG_MIN_NUM_LIGH_BITS = 4;
  private static final int LIGHTNING_REG_CL_STAT_BIT = 6;

  public LightningRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0x02, "Lightning");
  }

  public int getSpikeRejection() {
    return (registerValue >> LIGHTNING_REG_SREJ_BITS) & 0x0F;
  }

  public void setSpikeRejection(int rejection) throws IOException {
    registerValue &= ~((0x0F) << LIGHTNING_REG_SREJ_BITS);
    registerValue |= (rejection << LIGHTNING_REG_SREJ_BITS) & ((0x0F) << LIGHTNING_REG_SREJ_BITS);
    sensor.write(address, registerValue);
  }

  public int getMinNumLightning() {
    return (registerValue >> LIGHTNING_REG_MIN_NUM_LIGH_BITS) & 0x03;
  }

  public void setMinNumLightning(int numLightning) throws IOException {
    registerValue &= ~((0x03) << LIGHTNING_REG_MIN_NUM_LIGH_BITS);
    registerValue |= (numLightning << LIGHTNING_REG_MIN_NUM_LIGH_BITS) & ((0x03) << LIGHTNING_REG_MIN_NUM_LIGH_BITS);
    sensor.write(address, registerValue);
  }

  public boolean isClearStatisticsEnabled() {
    return ((registerValue & 0xff) & (1 << LIGHTNING_REG_CL_STAT_BIT)) != 0;
  }

  public void setClearStatisticsEnabled(boolean enabled) throws IOException {
    reload();
    if (enabled) {
      registerValue |= (1 << LIGHTNING_REG_CL_STAT_BIT);
    } else {
      registerValue &= ~(1 << LIGHTNING_REG_CL_STAT_BIT);
    }
    sensor.write(address, registerValue);
  }

  @Override
  public RegisterData toData() throws IOException {
    int spikeRejection = getSpikeRejection();
    int minNumLightning = getMinNumLightning();
    boolean clearStatisticsEnabled = isClearStatisticsEnabled();
    return new LightningData(spikeRejection, minNumLightning, clearStatisticsEnabled);
  }

  @Override
  public boolean fromData(RegisterData input) throws IOException {
    if (input instanceof LightningData data) {
      setSpikeRejection(data.getSpikeRejection());
      setMinNumLightning(data.getMinNumLightning());
      setClearStatisticsEnabled(data.isClearStatisticsEnabled());
      return true;
    }
    return false;
  }
}