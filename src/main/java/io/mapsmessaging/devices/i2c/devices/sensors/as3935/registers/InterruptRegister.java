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

public class InterruptRegister extends SingleByteRegister {

  private static final int ENERGY_MASK_DISTURBER_BIT = 5;
  private static final int ENERGY_DIV_RATIO_BITS = 6;

  public InterruptRegister(I2CDevice sensor) {
    super(sensor, 0x03);
  }

  public int getInterruptReason() throws IOException {
    return registerValue & 0xF;
  }

  public boolean isMaskDisturberEnabled() throws IOException {
    return (registerValue & (1 << ENERGY_MASK_DISTURBER_BIT)) != 0;
  }

  public void setMaskDisturberEnabled(boolean enabled) throws IOException {
    reload();
    if (enabled) {
      registerValue |= (1 << ENERGY_MASK_DISTURBER_BIT);
    } else {
      registerValue &= ~(1 << ENERGY_MASK_DISTURBER_BIT);
    }
    sensor.write(address, registerValue);
  }

  public int getEnergyDivRatio() throws IOException {
    return (registerValue >> ENERGY_DIV_RATIO_BITS) & 0x03;
  }

  public void setEnergyDivRatio(int divRatio) throws IOException {
    reload();
    registerValue &= ~((0x03) << ENERGY_DIV_RATIO_BITS);
    registerValue |= (divRatio << ENERGY_DIV_RATIO_BITS) & ((0x03) << ENERGY_DIV_RATIO_BITS);
    sensor.write(address, registerValue);
  }
}