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

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.as3935.data.InterruptData;
import io.mapsmessaging.devices.i2c.devices.sensors.as3935.values.InterruptReason;

import java.io.IOException;

public class InterruptRegister extends SingleByteRegister {

  private static final int ENERGY_MASK_DISTURBER_BIT = 5;
  private static final int ENERGY_DIV_RATIO_BITS = 6;

  public InterruptRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0x03, "Interrupt");
  }

  public InterruptReason getInterruptReason() {
    int mask = registerValue & 0xF;

    for (InterruptReason interruptReason : InterruptReason.values()) {
      if (interruptReason.getMask() == mask) {
        return interruptReason;
      }
    }
    return InterruptReason.NONE;
  }

  public boolean isMaskDisturberEnabled() {
    return ((registerValue & 0xff) & (1 << ENERGY_MASK_DISTURBER_BIT)) != 0;
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

  public int getEnergyDivRatio() {
    return (registerValue >> ENERGY_DIV_RATIO_BITS) & 0x03;
  }

  public void setEnergyDivRatio(int divRatio) throws IOException {
    reload();
    registerValue &= ~((0x03) << ENERGY_DIV_RATIO_BITS);
    registerValue |= (divRatio << ENERGY_DIV_RATIO_BITS) & ((0x03) << ENERGY_DIV_RATIO_BITS);
    sensor.write(address, registerValue);
  }

  @Override
  public RegisterData toData() throws IOException {
    InterruptReason interruptReason = getInterruptReason();
    boolean maskDisturberEnabled = isMaskDisturberEnabled();
    int energyDivRatio = getEnergyDivRatio();
    return new InterruptData(interruptReason, maskDisturberEnabled, energyDivRatio);
  }

  // Method to set InterruptRegister data from InterruptData
  @Override
  public boolean fromData(RegisterData input) throws IOException {
    if (input instanceof InterruptData) {
      InterruptData data = (InterruptData) input;
      setMaskDisturberEnabled(data.isMaskDisturberEnabled());
      setEnergyDivRatio(data.getEnergyDivRatio());
      return true;
    }
    return false;
  }
}