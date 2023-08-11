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

package io.mapsmessaging.devices.i2c.devices;

import io.mapsmessaging.devices.i2c.I2CDevice;

import java.io.IOException;

public class DualByteRegister extends Register {

  protected short registerValue;
  protected byte[] buffer;

  public DualByteRegister(I2CDevice sensor, int address, String name) throws IOException {
    super(sensor, address, name);
    buffer = new byte[2];
    reload();
  }

  @Override
  protected void reload() throws IOException {
    sensor.readRegister(address, buffer);
    registerValue = (short) (((buffer[1] & 0xff) << 8) | (buffer[0] & 0xff));
  }

  @Override
  protected void setControlRegister(int mask, int value) throws IOException {
    registerValue = (short) ((registerValue & mask) | value);
    buffer[0] = (byte) (registerValue & 0xff);
    buffer[1] = (byte) (registerValue >> 8 & 0xff);
    sensor.write(address, buffer);
  }


  public String toString(int length) {
    try {
      reload();
    } catch (IOException e) {
      // ignore, its a toString() function
    }
    return displayRegister(length, getAddress(), registerValue);
  }
}

