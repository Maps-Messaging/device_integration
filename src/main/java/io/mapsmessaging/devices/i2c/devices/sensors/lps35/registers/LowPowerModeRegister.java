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

package io.mapsmessaging.devices.i2c.devices.sensors.lps35.registers;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.lps35.data.LowPowerMode;

import java.io.IOException;

public class LowPowerModeRegister extends SingleByteRegister {

  private static final byte LC_EN = (byte) 0b00000001;

  public LowPowerModeRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0x1A, "RES_CONF");
    reload();
  }

  public boolean isLowCurrentMode() {
    return (registerValue & LC_EN) != 0;
  }

  public void setLowCurrentMode(boolean flag) throws IOException {
    setControlRegister(~LC_EN, flag ? LC_EN : 0);
  }

  @Override
  public RegisterData toData() {
    return new LowPowerMode(isLowCurrentMode());
  }

  @Override
  public boolean fromData(RegisterData input) throws IOException {
    if (input instanceof LowPowerMode) {
      LowPowerMode data = (LowPowerMode) input;
      setLowCurrentMode(data.isMode());
      return true;
    }
    return false;
  }

}