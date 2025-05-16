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

package io.mapsmessaging.devices.i2c.devices.sensors.lps25.registers;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.lps25.data.InterruptSourceData;
import io.mapsmessaging.devices.i2c.devices.sensors.lps25.values.InterruptSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InterruptSourceRegister extends SingleByteRegister {

  private static final byte INTERRUPT_SOURCE = 0x25;

  private static final byte BOOT_PHASE = (byte) 0b10000000;
  private static final byte INTERRUPT_ACTIVE = 0b00000100;
  private static final byte PRESSURE_LOW = 0b00000010;
  private static final byte PRESSURE_HIGH = 0b00000001;


  public InterruptSourceRegister(I2CDevice sensor) throws IOException {
    super(sensor, INTERRUPT_SOURCE, "INT_SOURCE");
    reload();
  }

  public InterruptSource[] getInterruptSource() throws IOException {
    reload();
    List<InterruptSource> sourceList = new ArrayList<>();
    if ((registerValue & BOOT_PHASE) != 0) {
      sourceList.add(InterruptSource.BOOT);
    }
    if ((registerValue & PRESSURE_HIGH) != 0) {
      sourceList.add(InterruptSource.PRESSURE_HIGH);
    }
    if ((registerValue & PRESSURE_LOW) != 0) {
      sourceList.add(InterruptSource.PRESSURE_LOW);
    }
    if ((registerValue & INTERRUPT_ACTIVE) != 0) {
      sourceList.add(InterruptSource.INTERRUPT_ACTIVE);
    }

    return sourceList.toArray(new InterruptSource[]{});
  }

  @Override
  public RegisterData toData() throws IOException {
    InterruptSourceData data = new InterruptSourceData();
    data.setInterruptSources(Arrays.asList(getInterruptSource()));
    return data;
  }

}