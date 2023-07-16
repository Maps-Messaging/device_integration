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

public class BufferedRegister extends Register {

  private final byte[] data;

  protected BufferedRegister(I2CDevice sensor, int address, byte[] data, String name, RegisterMap registerMap) {
    super(sensor, address, name, registerMap);
    this.data = data;
  }

  @Override
  protected void reload() throws IOException {
    // No Op, since it requires the entire buffer to be updated
  }

  @Override
  protected void setControlRegister(int mask, int value) throws IOException {
    data[address] = (byte) ((data[address] & mask) | value);
    sensor.write(address, data[address]);
  }

}
