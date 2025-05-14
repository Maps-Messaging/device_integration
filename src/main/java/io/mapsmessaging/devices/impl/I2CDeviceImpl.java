/*
 *
 *  Copyright [ 2020 - 2024 ] [Matthew Buckton]
 *  Copyright [ 2024 - 2025.  ] [Maps Messaging B.V.]
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package io.mapsmessaging.devices.impl;

import com.pi4j.io.i2c.I2C;

public class I2CDeviceImpl implements AddressableDevice {

  private final I2C i2c;

  public I2CDeviceImpl(I2C i2c) {
    this.i2c = i2c;
  }

  @Override
  public void close() {
    i2c.close();
  }

  @Override
  public int getBus() {
    return i2c.getBus();
  }

  @Override
  public int write(int val) {
    return i2c.write(val);
  }

  @Override
  public int write(byte[] buffer, int offset, int length) {
    return i2c.write(buffer, offset, length);
  }

  @Override
  public int writeRegister(int register, byte[] data) {
    return i2c.writeRegister(register, data);
  }

  @Override
  public int read(byte[] buffer, int offset, int length) {
    return i2c.read(buffer, offset, length);
  }

  @Override
  public int readRegister(int register) {
    return i2c.readRegister(register);
  }

  @Override
  public int readRegister(int register, byte[] buffer, int offset, int length) {
    return i2c.readRegister(register, buffer, offset, length);
  }

  @Override
  public int getDevice() {
    return i2c.getDevice();
  }

  @Override
  public int read() {
    return i2c.read();
  }
}
