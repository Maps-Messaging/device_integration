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

import java.io.IOException;

public class Control4 extends Register {

  private static final byte CONTROL_REGISTER4 = 0x23;

  public Control4(Lps25Sensor sensor) throws IOException {
    super(sensor, CONTROL_REGISTER4);
    reload();
  }

  public void enabledFiFoEmptyInterrupt(boolean flag) throws IOException {
    int value = flag ? 0b001000 : 0;
    setControlRegister(0b11110111, value);
  }

  public boolean isFiFoEmptyEnabled() throws IOException {
    return (registerValue & 0b001000) != 0;
  }

  public void enableFiFoWatermarkInterrupt(boolean flag) throws IOException {
    int value = flag ? 0b00100 : 0;
    setControlRegister(0b11111011, value);
  }

  public boolean isFiFoWatermarkInterruptEnabled() throws IOException {
    return (registerValue & 0b00100) != 0;
  }

  public void enableFiFoOverrunInterrupt(boolean flag) throws IOException {
    int value = flag ? 0b0010 : 0;
    setControlRegister(0b11111101, value);
  }

  public boolean isFiFoOverrunInterruptEnabled() throws IOException {
    return (registerValue & 0b0010) != 0;
  }

  public void setDataReadyInterrupt(boolean flag) throws IOException {
    int value = flag ? 0b001 : 0;
    setControlRegister(0b11111110, value);
  }

  public boolean isDataReadyInterrupt() throws IOException {
    return (registerValue & 0b001) != 0;
  }
}
