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

package io.mapsmessaging.devices.i2c.devices.sensors.bme688.register;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.MultiByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.data.HeatResistance;

import java.io.IOException;

public class HeaterResistanceRegister extends MultiByteRegister {

  public HeaterResistanceRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0x5A, 10, "res_heat");
    reload();
  }

  public byte[] getHeaterResistance() {
    return super.buffer;
  }

  public void setHeaterResistance(int idx, byte val) throws IOException {
    buffer[idx] = val;
    sensor.write(address + idx, val);
  }

  @Override
  public RegisterData toData() throws IOException {
    getHeaterResistance();
    int[] value = new int[buffer.length];
    for (int x = 0; x < value.length; x++) {
      value[x] = (buffer[x] & 0xff);
    }
    return new HeatResistance(value);
  }
}

