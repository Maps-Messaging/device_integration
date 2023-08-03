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

  public void setRate(int on, int off) throws IOException {
    writeVal(on, off);
  }

  public int getOff() throws IOException {
    return readVal(2);
  }

  @Override
  public boolean fromData(RegisterData input) throws IOException {
    if (input instanceof LedControlData) {
      LedControlData data = (LedControlData) input;
      setRate(data.getOn(), data.getOff());
      return true;
    }
    return false;
  }

  @Override
  public RegisterData toData() throws IOException {
    return new LedControlData(getOn(), getOff());
  }

  protected void writeVal(int on, int off) throws IOException {
    byte[] b = new byte[4];
    b[0] = (byte) (on & 0xff);
    b[1] = (byte) ((on >> 8) & 0x0f);
    b[2] = (byte) (off & 0xff);
    b[3] = (byte) ((off >> 8) & 0x0f);
    sensor.write(address, b);
  }

  protected int readVal(int offset) throws IOException {
    byte[] b = new byte[2];
    sensor.readRegister(address + offset, b);
    return b[0] & 0xff | ((b[1] & 0xf) << 8);
  }
}
