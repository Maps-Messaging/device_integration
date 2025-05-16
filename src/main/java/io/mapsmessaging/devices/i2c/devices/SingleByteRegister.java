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

package io.mapsmessaging.devices.i2c.devices;

import io.mapsmessaging.devices.i2c.I2CDevice;
import lombok.Getter;

import java.io.IOException;

@Getter
public class SingleByteRegister extends Register {

  protected byte registerValue;

  public SingleByteRegister(I2CDevice sensor, int address, String name) throws IOException {
    super(sensor, address, name);
    reload();
  }

  @Override
  protected void reload() throws IOException {
    registerValue = (byte) (sensor.readRegister(address) & 0Xff);
  }

  @Override
  protected void setControlRegister(int mask, int value) throws IOException {
    registerValue = (byte) ((registerValue & mask) | value);
    sensor.write(address, registerValue);
  }

  public void read() throws IOException {
    reload();
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
