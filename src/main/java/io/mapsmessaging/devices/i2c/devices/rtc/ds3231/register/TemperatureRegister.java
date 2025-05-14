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

package io.mapsmessaging.devices.i2c.devices.rtc.ds3231.register;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.MultiByteRegister;
import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.data.TemperatureData;

import java.io.IOException;

public class TemperatureRegister extends MultiByteRegister {

  public TemperatureRegister(I2CDevice sensor) {
    super(sensor, 0x11, 2, "TEMPERATURE");
  }

  public float getTemperature() throws IOException {
    reload();
    int tempValue = ((buffer[0] & 0x7F) << 2) + ((buffer[1] >> 6) & 0x03);
    return tempValue / 4.0f;
  }

  @Override
  public RegisterData toData() throws IOException {
    return new TemperatureData(getTemperature());
  }

}