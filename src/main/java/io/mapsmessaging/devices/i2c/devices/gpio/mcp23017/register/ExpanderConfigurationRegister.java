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

package io.mapsmessaging.devices.i2c.devices.gpio.mcp23017.register;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;

import java.io.IOException;

public class ExpanderConfigurationRegister extends SingleByteRegister {

  private static final int MIRROR = 0b01000000;
  private static final int SEQOP = 0b00100000;
  private static final int DISSLW = 0b00010000;
  private static final int HAEN = 0b00001000;
  private static final int ODR = 0b00000100;
  private static final int INTPOL = 0b00000010;


  public ExpanderConfigurationRegister(I2CDevice sensor, byte address) throws IOException {
    super(sensor, address, "IOCON");
    reload();
  }

  public void clear() throws IOException {
    registerValue = (byte) 0x0;
    sensor.write(address, registerValue);
  }

  public boolean isMirror() {
    return (registerValue & MIRROR) != 0;
  }

  public void setMirror(boolean flag) throws IOException {
    super.setControlRegister(~MIRROR, flag ? MIRROR : 0);
  }

  public boolean isSequential() {
    return (registerValue & SEQOP) == 0;
  }

  public void setSequential(boolean flag) throws IOException {
    super.setControlRegister(~SEQOP, flag ? 0 : SEQOP);
  }

  public boolean isOpenDrain() {
    return (registerValue & ODR) != 0;
  }

  public void setOpenDrain(boolean flag) throws IOException {
    super.setControlRegister(~ODR, flag ? ODR : 0);
  }

  public boolean getPolarity() {
    return (registerValue & INTPOL) != 0;
  }

  public void setPolarity(boolean flag) throws IOException {
    super.setControlRegister(~INTPOL, flag ? INTPOL : 0);
  }

}