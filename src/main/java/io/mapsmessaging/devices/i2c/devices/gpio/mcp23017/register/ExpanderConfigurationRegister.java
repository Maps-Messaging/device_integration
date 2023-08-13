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
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;

import java.io.IOException;

public class ExpanderConfigurationRegister extends SingleByteRegister {

  private final int MIRROR = 0b01000000;
  private final int SEQOP = 0b00100000;
  private final int DISSLW = 0b00010000;
  private final int HAEN = 0b00001000;
  private final int ODR = 0b00000100;
  private final int INTPOL = 0b00000010;


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