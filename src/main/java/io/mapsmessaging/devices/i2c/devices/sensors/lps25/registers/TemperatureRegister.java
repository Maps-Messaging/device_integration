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

package io.mapsmessaging.devices.i2c.devices.sensors.lps25.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.MultiByteRegister;

import java.io.IOException;

public class TemperatureRegister extends MultiByteRegister {
  public TemperatureRegister(I2CDevice sensor) {
    super(sensor, 0x2B | 0x80, 2, "TEMP_OUT");
  }

  @Override
  public int getAddress(){
    return address & (~0x80);
  }

  public float getTemperature() throws IOException {
    reload();
    int raw = asInt();
    if ((raw & 0x8000) != 0) {
      raw = raw - 0xFFFF;
    }
    return raw / 480.0f + 42.5f;
  }
}

