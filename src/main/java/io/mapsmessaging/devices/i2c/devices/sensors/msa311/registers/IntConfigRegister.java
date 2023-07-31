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

package io.mapsmessaging.devices.i2c.devices.sensors.msa311.registers;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.data.IntConfigData;

import java.io.IOException;

public class IntConfigRegister extends SingleByteRegister {

  private static final byte INT1_OD = (byte) 0b00000001;
  private static final byte INT1_LVL = (byte) 0b00000010;

  public IntConfigRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0x20, "Int_Config");
  }

  public void setInt1OutputType(boolean openDrain) throws IOException {
    int value = openDrain ? INT1_OD : 0;
    setControlRegister(~INT1_OD, value);
  }

  public boolean isInt1OutputTypeOpenDrain() {
    return (registerValue & INT1_OD) != 0;
  }

  public void setInt1ActiveLevel(boolean highLevel) throws IOException {
    int value = highLevel ? INT1_LVL : 0;
    setControlRegister(~INT1_LVL, value);
  }

  public boolean isInt1ActiveLevelHigh() {
    return (registerValue & INT1_LVL) != 0;
  }

  @Override
  public RegisterData toData() throws IOException {
    return new IntConfigData(isInt1OutputTypeOpenDrain(), isInt1ActiveLevelHigh());
  }

  @Override
  public boolean fromData(RegisterData input) throws IOException {
    if (input instanceof IntConfigData) {
      IntConfigData data = (IntConfigData) input;
      setInt1OutputType(data.isInt1OutputTypeOpenDrain());
      setInt1ActiveLevel(data.isInt1ActiveLevelHigh());
      return true;
    }
    return false;
  }
}

