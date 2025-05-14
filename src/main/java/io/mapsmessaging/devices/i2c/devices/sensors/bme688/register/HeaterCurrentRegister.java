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
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.data.HeaterCurrent;

import java.io.IOException;

public class HeaterCurrentRegister extends MultiByteRegister {

  public HeaterCurrentRegister(I2CDevice sensor) {
    super(sensor, 0x50, 10, "idac_heat");
  }

  public byte[] getHeaterCurrent() throws IOException {
    reload();
    return super.buffer;
  }

  public void setHeaterCurrent(byte[] val) throws IOException {
    System.arraycopy(val, 0, buffer, 0, buffer.length);
    sensor.write(address, buffer);
  }

  @Override
  public RegisterData toData() throws IOException {
    getHeaterCurrent();
    int[] value = new int[buffer.length];
    for (int x = 0; x < value.length; x++) {
      value[x] = (buffer[x] & 0xff);
    }
    return new HeaterCurrent(value);
  }
}

