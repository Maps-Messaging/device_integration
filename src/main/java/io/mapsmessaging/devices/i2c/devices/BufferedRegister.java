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
  private final int length;

  public BufferedRegister(I2CDevice sensor, int address, String name, byte[] data) {
    this(sensor, address, 1, name, data);
  }


  public BufferedRegister(I2CDevice sensor, int address, int length, String name, byte[] data) {
    super(sensor, address, name);
    this.data = data;
    this.length = length;
  }

  public int getValueReverse(){
    int val = 0;
    for (int x = 0; x < length; x++) {
      val = val << 8;
      val |= data[address+x] & 0xff;
    }
    return val;
  }


  public int getValue(){
    int val = 0;
    for (int x = length - 1; x >= 0; x--) {
      val = val << 8;
      val |= data[address+x] & 0xff;
    }
    return val;
  }

  @Override
  protected void reload() throws IOException {
    // No Op, since it requires the entire buffer to be updated
  }

  @Override
  protected void setControlRegister(int mask, int value) throws IOException {
    data[getAddress()] = (byte) ((data[getAddress()] & mask) | value);
    sensor.write(getAddress(), data[getAddress()]);
  }

  @Override
  public String toString(int len) {
    StringBuilder stringBuilder = new StringBuilder();
    for(int x=0; x<length;x++){
      if (x != 0) stringBuilder.append("\t");
      stringBuilder.append(displayRegister(len, getAddress() + x, data[getAddress()+x]));
      if (x < length-1) stringBuilder.append("\n");
    }
    return stringBuilder.toString();
  }
}
