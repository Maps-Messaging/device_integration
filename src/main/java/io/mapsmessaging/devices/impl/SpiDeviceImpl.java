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

package io.mapsmessaging.devices.impl;

import com.pi4j.io.spi.Spi;

public class SpiDeviceImpl implements AddressableDevice {

  private final Spi spi;

  public SpiDeviceImpl(Spi spi) {
    this.spi = spi;
  }

  @Override
  public void close() {
    spi.close();
  }

  @Override
  public int getBus() {
    return 0;
  }

  @Override
  public int write(int val) {
    return spi.write(val);
  }

  @Override
  public int write(byte[] buffer, int offset, int length) {
    return 0;
  }

  @Override
  public int writeRegister(int register, byte[] data) {
    return 0;
  }

  @Override
  public int read(byte[] buffer, int offset, int length) {
    return spi.read(buffer, offset, length);
  }

  @Override
  public int readRegister(int register) {
    return spi.read();
  }

  @Override
  public int readRegister(int register, byte[] buffer, int offset, int length) {
    return 0;
  }

  @Override
  public int getDevice() {
    return 0;
  }

  @Override
  public int read() {
    return 0;
  }
}
