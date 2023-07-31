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

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.lps25.data.Control3Data;
import io.mapsmessaging.devices.i2c.devices.sensors.lps25.values.DataReadyInterrupt;

import java.io.IOException;

public class Control3 extends SingleByteRegister {

  private static final byte CONTROL_REGISTER3 = 0x22;

  private static final byte INTERRUPT_ACTIVE = (byte) 0b10000000;
  private static final byte PUSH_PULL_DRAIN = 0b01000000;
  private static final byte INTERRUPT_SIGNAL = 0b00000011;

  public Control3(I2CDevice sensor) throws IOException {
    super(sensor, CONTROL_REGISTER3, "CTRL_REG3");
    reload();
  }

  public boolean isInterruptActive() {
    return (registerValue & INTERRUPT_ACTIVE) != 0;
  }

  public void enableInterrupts(boolean flag) throws IOException {
    int value = flag ? INTERRUPT_ACTIVE : 0;
    setControlRegister(~INTERRUPT_ACTIVE, value);
  }

  public boolean isPushPullDrainInterruptActive() {
    return (registerValue & PUSH_PULL_DRAIN) != 0;
  }

  public void enablePushPullDrainInterrupt(boolean flag) throws IOException {
    int value = flag ? PUSH_PULL_DRAIN : 0;
    setControlRegister(~PUSH_PULL_DRAIN, value);
  }

  public void setSignalOnInterrupt(DataReadyInterrupt flag) throws IOException {
    setControlRegister(~INTERRUPT_SIGNAL, flag.getMask());
  }

  public DataReadyInterrupt isSignalOnInterrupt() {
    int mask = (registerValue & INTERRUPT_SIGNAL);
    for (DataReadyInterrupt dataReadyInterrupt : DataReadyInterrupt.values()) {
      if (mask == dataReadyInterrupt.getMask()) {
        return dataReadyInterrupt;
      }
    }
    return DataReadyInterrupt.ORDER_OF_PRIORITY;
  }

  @Override
  public RegisterData toData() {
    return new Control3Data(isInterruptActive(), isPushPullDrainInterruptActive(), isSignalOnInterrupt());
  }

  @Override
  public boolean fromData(RegisterData input) throws IOException {
    if (input instanceof Control3Data) {
      Control3Data data = (Control3Data) input;
      setSignalOnInterrupt(data.getSignalOnInterrupt());
      enablePushPullDrainInterrupt(data.isPushPullDrainInterruptActive());
      enableInterrupts(data.isInterruptActive());
      return true;
    }
    return false;
  }
}
