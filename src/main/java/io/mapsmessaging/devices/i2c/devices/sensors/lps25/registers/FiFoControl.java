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
import io.mapsmessaging.devices.i2c.devices.sensors.lps25.values.FiFoMode;

import java.io.IOException;

public class FiFoControl extends Register {

  private static final int FIFO_CONTROL = 0x2E;

  public FiFoControl(Lps25Sensor sensor) throws IOException {
    super(sensor, FIFO_CONTROL);
    reload();
  }

  public FiFoMode getFifoMode() throws IOException {
    int mask = registerValue >> 5;
    for (FiFoMode mode : FiFoMode.values()) {
      if (mode.getMask() == mask) {
        return mode;
      }
    }
    return FiFoMode.BYPASS;
  }

  public void setFifoMode(FiFoMode mode) throws IOException {
    setControlRegister(0b11111, mode.getMask());
  }

  public int getFiFoWaterMark() throws IOException {
    return (registerValue & 0b11111);
  }

  public void setFiFoWaterMark(int waterMark) throws IOException {
    setControlRegister(0b11100000, (waterMark & 0b11111));
  }

}
