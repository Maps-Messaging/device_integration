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

package io.mapsmessaging.devices.i2c.devices.sensors.bno055.registers;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.bno055.data.SystemStatusData;
import io.mapsmessaging.devices.i2c.devices.sensors.bno055.values.SystemStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SystemStatusRegister extends SingleByteRegister {

  private long lastRead;

  public SystemStatusRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0X39, "SYS_STATUS");
    lastRead = System.currentTimeMillis();
  }

  public List<SystemStatus> getStatus() throws IOException {
    check();
    List<SystemStatus> list = new ArrayList<>();
    for (SystemStatus status : SystemStatus.values()) {
      int bit = 1 << status.ordinal();
      if ((registerValue & bit) != 0) {
        list.add(status);
      }
    }
    return list;
  }

  private void check() throws IOException {
    if (System.currentTimeMillis() < lastRead) {
      reload();
      lastRead = System.currentTimeMillis() + 100;
    }
  }

  public RegisterData toData() throws IOException {
    return new SystemStatusData(getStatus());
  }

}
