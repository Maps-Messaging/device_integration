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

import java.io.IOException;

public class Control4 extends Register {

  private static final byte CONTROL_REGISTER4 = 0x23;

  private static final byte FIFO_EMPTY = 0b00001000;
  private static final byte FIFO_THRESHOLD = 0b00000100;
  private static final byte FIFO_OVERFLOW = 0b00000010;
  private static final byte DATA_READY = 0b00000001;

  public Control4(I2CDevice sensor) throws IOException {
    super(sensor, CONTROL_REGISTER4);
    reload();
  }

  public void enabledFiFoEmptyInterrupt(boolean flag) throws IOException {
    int value = flag ? FIFO_EMPTY : 0;
    setControlRegister(~FIFO_EMPTY, value);
  }

  public boolean isFiFoEmptyEnabled() throws IOException {
    return (registerValue & FIFO_EMPTY) != 0;
  }

  public void enableFiFoWatermarkInterrupt(boolean flag) throws IOException {
    int value = flag ? FIFO_THRESHOLD : 0;
    setControlRegister(~FIFO_THRESHOLD, value);
  }

  public boolean isFiFoWatermarkInterruptEnabled() throws IOException {
    return (registerValue & FIFO_THRESHOLD) != 0;
  }

  public void enableFiFoOverrunInterrupt(boolean flag) throws IOException {
    int value = flag ? FIFO_OVERFLOW : 0;
    setControlRegister(~FIFO_OVERFLOW, value);
  }

  public boolean isFiFoOverrunInterruptEnabled() throws IOException {
    return (registerValue & FIFO_OVERFLOW) != 0;
  }

  public boolean isDataReadyInterrupt() throws IOException {
    return (registerValue & DATA_READY) != 0;
  }

  public void setDataReadyInterrupt(boolean flag) throws IOException {
    int value = flag ? DATA_READY : 0;
    setControlRegister(~DATA_READY, value);
  }
}
