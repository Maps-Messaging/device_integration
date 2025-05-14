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

package io.mapsmessaging.devices.i2c.devices.sensors.lps35.registers;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.lps35.data.Control3Data;
import io.mapsmessaging.devices.i2c.devices.sensors.lps35.values.DataReadyInterrupt;

import java.io.IOException;

public class Control3Register extends SingleByteRegister {

  private static final int INTERRUPT_ACTIVE = 0b10000000;
  private static final int PUSH_DRAIN_ACTIVE = 0b01000000;
  private static final int FIFO_DRAIN_INTERRUPT = 0b00100000;
  private static final int WATER_MARK_INTERRUPT = 0b0010000;
  private static final int FIFO_OVERRUN_INTERRUPT = 0b001000;
  private static final int SIGNAL_ON_INTERRUPT = 0b00000011;

  public Control3Register(I2CDevice sensor) throws IOException {
    super(sensor, 0x12, "CTRL_REG3");
    reload();
  }

  public boolean isInterruptActive() {
    return (registerValue & INTERRUPT_ACTIVE) != 0;
  }

  public boolean isPushPullDrainActive() {
    return (registerValue & PUSH_DRAIN_ACTIVE) != 0;
  }

  public void enableFiFoDrainInterrupt(boolean flag) throws IOException {
    setControlRegister(~FIFO_DRAIN_INTERRUPT, flag ? FIFO_DRAIN_INTERRUPT : 0);
  }

  public boolean isFiFoDrainInterruptEnabled() {
    return (registerValue & FIFO_DRAIN_INTERRUPT) != 0;
  }

  public void enableFiFoWatermarkInterrupt(boolean flag) throws IOException {
    setControlRegister(~WATER_MARK_INTERRUPT, flag ? WATER_MARK_INTERRUPT : 0);
  }

  public boolean isFiFoWatermarkInterruptEnabled() {
    return (registerValue & WATER_MARK_INTERRUPT) != 0;
  }

  public void enableFiFoOverrunInterrupt(boolean flag) throws IOException {
    setControlRegister(~FIFO_OVERRUN_INTERRUPT, flag ? FIFO_OVERRUN_INTERRUPT : 0);
  }

  public boolean isFiFoOverrunInterruptEnabled() {
    return (registerValue & FIFO_OVERRUN_INTERRUPT) != 0;
  }

  public void setSignalOnInterrupt(DataReadyInterrupt flag) throws IOException {
    setControlRegister(~SIGNAL_ON_INTERRUPT, flag.getMask());
  }

  public DataReadyInterrupt isSignalOnInterrupt() {
    int mask = (registerValue & SIGNAL_ON_INTERRUPT);
    for (DataReadyInterrupt dataReadyInterrupt : DataReadyInterrupt.values()) {
      if (mask == dataReadyInterrupt.getMask()) {
        return dataReadyInterrupt;
      }
    }
    return DataReadyInterrupt.ORDER_OF_PRIORITY;
  }

  @Override
  public RegisterData toData() throws IOException {
    boolean fiFoDrainInterruptEnabled = isFiFoDrainInterruptEnabled();
    boolean fiFoWatermarkInterruptEnabled = isFiFoWatermarkInterruptEnabled();
    boolean fiFoOverrunInterruptEnabled = isFiFoOverrunInterruptEnabled();
    boolean isInterrupt = isInterruptActive();
    boolean drianInterrupted = isPushPullDrainActive();
    DataReadyInterrupt signalOnInterrupt = isSignalOnInterrupt();
    return new Control3Data(isInterrupt, drianInterrupted, fiFoDrainInterruptEnabled, fiFoWatermarkInterruptEnabled, fiFoOverrunInterruptEnabled, signalOnInterrupt);
  }

  // Method to set Control3Register data from Control3Data
  @Override
  public boolean fromData(RegisterData input) throws IOException {
    if (input instanceof Control3Data) {
      Control3Data data = (Control3Data) input;
      enableFiFoDrainInterrupt(data.isFiFoDrainInterruptEnabled());
      enableFiFoWatermarkInterrupt(data.isFiFoWatermarkInterruptEnabled());
      enableFiFoOverrunInterrupt(data.isFiFoOverrunInterruptEnabled());
      setSignalOnInterrupt(data.getSignalOnInterrupt());
      return true;
    }
    return false;
  }

}