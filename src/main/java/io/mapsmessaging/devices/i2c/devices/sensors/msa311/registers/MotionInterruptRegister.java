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

package io.mapsmessaging.devices.i2c.devices.sensors.msa311.registers;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.data.MotionInterruptData;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.values.MotionInterrupts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MotionInterruptRegister extends SingleByteRegister {

  public MotionInterruptRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0x9, "Motion_Interrupt");
  }

  public List<MotionInterrupts> getInterrupts() throws IOException {
    List<MotionInterrupts> list = new ArrayList<>();
    reload();
    for (MotionInterrupts interrupts : MotionInterrupts.values()) {
      if ((interrupts.getMask() & registerValue) != 0) {
        list.add(interrupts);
      }
    }
    return list;
  }

  @Override
  public RegisterData toData() throws IOException {
    return new MotionInterruptData(getInterrupts());
  }

}
