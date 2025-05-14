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

package io.mapsmessaging.devices.i2c.devices.sensors.lps25.registers;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.lps25.data.StatusData;
import io.mapsmessaging.devices.i2c.devices.sensors.lps25.values.Status;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StatusRegister extends SingleByteRegister {

  private static final byte STATUS_REGISTER = 0x27;

  private static final byte PRESSURE_OVERRUN = 0b100000;
  private static final byte TEMPERATURE_OVERRUN = 0b010000;
  private static final byte PRESSURE_DATA_AVAILABLE = 0b000010;
  private static final byte TEMPERATURE_DATA_AVAILABLE = 0b000001;

  public StatusRegister(I2CDevice sensor) throws IOException {
    super(sensor, STATUS_REGISTER, "STATUS_REG");
    reload();
  }

  public boolean isTemperatureDataAvailable() throws IOException {
    reload();
    return (registerValue & TEMPERATURE_DATA_AVAILABLE) != 0;
  }

  public boolean isPressureDataAvailable() throws IOException {
    reload();
    return (registerValue & PRESSURE_DATA_AVAILABLE) != 0;
  }

  public Status[] getStatus() throws IOException {
    reload();
    List<Status> sourceList = new ArrayList<>();
    if ((registerValue & TEMPERATURE_OVERRUN) != 0) {
      sourceList.add(Status.TEMPERATURE_OVERRUN);
    }
    if ((registerValue & PRESSURE_OVERRUN) != 0) {
      sourceList.add(Status.PRESSURE_OVERRUN);
    }
    if (isTemperatureDataAvailable()) {
      sourceList.add(Status.TEMPERATURE_DATA_AVAILABLE);
    }
    if (isPressureDataAvailable()) {
      sourceList.add(Status.PRESSURE_DATA_AVAILABLE);
    }
    return sourceList.toArray(new Status[]{});
  }

  public RegisterData toData() throws IOException {
    return new StatusData(Arrays.asList(getStatus()));
  }


}