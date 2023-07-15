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

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;

import java.io.IOException;

public class FreefallHyRegister extends SingleByteRegister {

  private static final byte FREEFALL_MODE = (byte) 0b00000100;
  private static final byte HYSTERESIS_MASK = 0b00000011;

  public FreefallHyRegister(I2CDevice sensor) {
    super(sensor, 0x24);
  }

  public boolean isFreefallModeEnabled() throws IOException {
    reload();
    return (registerValue & FREEFALL_MODE) != 0;
  }

  public void setFreefallMode(boolean enable) throws IOException {
    if (enable) {
      registerValue |= FREEFALL_MODE;
    } else {
      registerValue &= ~FREEFALL_MODE;
    }
    sensor.write(address, registerValue);
  }

  public int getHysteresis() throws IOException {
    reload();
    return registerValue & HYSTERESIS_MASK;
  }

  public void setHysteresis(int hysteresis) throws IOException {
    int value = registerValue & ~HYSTERESIS_MASK;
    value |= hysteresis & HYSTERESIS_MASK;
    sensor.write(address, (byte) value);
  }
}
