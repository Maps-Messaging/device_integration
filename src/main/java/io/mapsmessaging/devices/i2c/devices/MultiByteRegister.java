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
import lombok.Getter;

import java.io.IOException;

public class MultiByteRegister extends Register {

  @Getter
  private final byte[] buffer;

  public MultiByteRegister(I2CDevice sensor, int address, int size, String name, RegisterMap registerMap) {
    super(sensor, address, name, registerMap);
    buffer = new byte[size];
  }

  @Override
  protected void reload() throws IOException {
    sensor.readRegister(address, buffer);
  }

  @Override
  protected void setControlRegister(int mask, int value) throws IOException {
    throw new IOException("Function not supported");
  }

  protected void write(int val) throws IOException {
    for (int x = buffer.length - 1; x >= 0; x--) {
      buffer[x] = (byte) (val & 0xff);
      val = val >> 8;
    }
    sensor.write(address, buffer);
  }

  protected void write(long val) throws IOException {
    for (int x = buffer.length - 1; x >= 0; x--) {
      buffer[x] = (byte) (val & 0xff);
      val = val >> 8;
    }
    sensor.write(address, buffer);
  }


  protected int asInt() {
    return (int) asLong();
  }

  protected long asLong() {
    long val = 0;
    for (int x = buffer.length - 1; x >= 0; x--) {
      val = val << 8;
      val |= buffer[x] & 0xff;
    }
    return val;
  }
}
