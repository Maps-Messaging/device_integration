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

import io.mapsmessaging.devices.i2c.devices.sensors.lps25.Lps25Sensor;
import io.mapsmessaging.devices.i2c.devices.sensors.lps25.values.DataReadyInterrupt;

import java.io.IOException;

public class Control3 extends Register {

  private static final byte CONTROL_REGISTER3 = 0x22;

  public Control3(Lps25Sensor sensor) throws IOException {
    super(sensor, CONTROL_REGISTER3);
    reload();
  }

  public boolean isInterruptActive() throws IOException {
    return (registerValue & 0b10000000) != 0;
  }

  public boolean isPushPullDrainActive() throws IOException {
    return (registerValue & 0b01000000) != 0;
  }

  public void setSignalOnInterrupt(DataReadyInterrupt flag) throws IOException {
    int value = flag.getMask();
    setControlRegister(0b11111100, value);
  }

  public DataReadyInterrupt isSignalOnInterrupt() throws IOException {
    int mask = (registerValue & 0b11);
    for (DataReadyInterrupt dataReadyInterrupt : DataReadyInterrupt.values()) {
      if (mask == dataReadyInterrupt.getMask()) {
        return dataReadyInterrupt;
      }
    }
    return DataReadyInterrupt.ORDER_OF_PRIORITY;
  }

}
