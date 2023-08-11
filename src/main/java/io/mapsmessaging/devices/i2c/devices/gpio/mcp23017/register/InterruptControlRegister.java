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

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.DualByteRegister;

import java.io.IOException;

public class InterruptControlRegister extends DualByteRegister {

  public InterruptControlRegister(I2CDevice sensor) throws IOException {
    super(sensor, (byte) 4, "GPINTEN");
    reload();
  }

  public void interruptEnable(int pin, boolean enable) {
    int reg = 1 << pin;
    registerValue = (byte) (registerValue & (enable ? reg : ~reg));
  }

  public boolean isEnabled(int pin) {
    return (registerValue & (1 << pin) & 0xff) != 0;
  }

  public RegisterData toData() throws IOException {
    return null;
  }
}
