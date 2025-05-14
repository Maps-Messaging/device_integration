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
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.data.AgingData;

import java.io.IOException;

public class AgingRegister extends SingleByteRegister {

  public AgingRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0x10, "AGING");
  }

  public int getAging() throws IOException {
    reload();
    return registerValue;
  }

  public void setAging(int aging) throws IOException {
    sensor.write(address, (byte) aging);
  }

  @Override
  public boolean fromData(RegisterData input) throws IOException {
    if (input instanceof AgingData) {
      AgingData data = (AgingData) input;
      setAging(data.getAging());
      return true;
    }
    return false;
  }

  @Override
  public RegisterData toData() throws IOException {
    return new AgingData(getAging());
  }
}