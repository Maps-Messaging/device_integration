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

package io.mapsmessaging.devices.i2c.devices.drivers.pca9685.registers;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.MultiByteRegister;
import io.mapsmessaging.devices.i2c.devices.drivers.pca9685.data.LedControlData;

import java.io.IOException;

public class LedControlRegister extends MultiByteRegister {

  public LedControlRegister(I2CDevice sensor, int address, String name) {
    super(sensor, address, 4, name);
  }

  public int getOn() throws IOException {
    return readVal(2);
  }

  public void setOn(int on) throws IOException {
    writeVal(0, on);
  }

  public int getOff() throws IOException {
    return readVal(2);
  }

  public void setOff(int off) throws IOException {
    writeVal(2, off);
  }

  @Override
  public boolean fromData(RegisterData input) throws IOException {
    if (input instanceof LedControlData) {
      LedControlData data = (LedControlData) input;
      setOn(data.getOn());
      setOff(data.getOff());
      return true;
    }
    return false;
  }

  @Override
  public RegisterData toData() throws IOException {
    return new LedControlData(getOn(), getOff());
  }

  protected void writeVal(int offset, int val) throws IOException {
    byte[] b = new byte[2];
    b[0] = (byte) (val & 0xff);
    b[1] = (byte) ((val >> 8) & 0x0f);
    sensor.write(address + offset, b);
  }

  protected int readVal(int offset) throws IOException {
    byte[] b = new byte[2];
    sensor.readRegister(address + offset, b);
    return b[0] & 0xff | ((b[1] & 0xf) << 8);
  }
}
