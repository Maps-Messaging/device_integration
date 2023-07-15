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

public class InterruptSet1Register extends SingleByteRegister {

  private static final byte NEW_DATA_INT_EN = (byte) 0b10000;
  private static final byte FREEFALL_INT_EN = (byte) 0b01000;

  public InterruptSet1Register(I2CDevice sensor) {
    super(sensor, 0x17);
  }

  public void setNewDataInterruptEnabled(boolean enabled) throws IOException {
    int value = enabled ? NEW_DATA_INT_EN : 0;
    setControlRegister(~NEW_DATA_INT_EN, value);
  }

  public boolean isNewDataInterruptEnabled() {
    return (registerValue & NEW_DATA_INT_EN) != 0;
  }

  public void setFreefallInterruptEnabled(boolean enabled) throws IOException {
    int value = enabled ? FREEFALL_INT_EN : 0;
    setControlRegister(~FREEFALL_INT_EN, value);
  }

  public boolean isFreefallInterruptEnabled() {
    return (registerValue & FREEFALL_INT_EN) != 0;
  }
}

