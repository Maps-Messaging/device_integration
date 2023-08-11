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

package io.mapsmessaging.devices.i2c.devices.gpio.mcp23017.register;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.DualByteRegister;

import java.io.IOException;

public abstract class GenericPinConfigRegister extends DualByteRegister {

  public GenericPinConfigRegister(I2CDevice sensor, int address, String name) throws IOException {
    super(sensor, address, name);
    reload();
  }

  public void change(int pin, boolean set) throws IOException {
    if (pin < 0 || pin > 16) {
      throw new IOException("Outside range");
    }
    int reg = 1 << pin;
    registerValue = (byte) (registerValue & (set ? reg : ~reg));
  }

  public boolean isSet(int pin) {
    return (registerValue & (1 << pin)) != 0;
  }
}