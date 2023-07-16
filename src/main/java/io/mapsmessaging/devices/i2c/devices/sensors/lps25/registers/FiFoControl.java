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
import io.mapsmessaging.devices.i2c.devices.RegisterMap;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.lps25.values.FiFoMode;

import java.io.IOException;

public class FiFoControl extends SingleByteRegister {

  private static final byte FIFO_CONTROL = 0x2E;
  private static final byte FIFO_MODE = (byte) 0b11100000;
  private static final byte FIFO_THRESHOLD = 0b00011111;

  public FiFoControl(I2CDevice sensor, RegisterMap registerMap) throws IOException {
    super(sensor, FIFO_CONTROL, "FiFo Control", registerMap);
    reload();
  }

  public FiFoMode getFifoMode() throws IOException {
    int mask = (registerValue & 0xff) >> 5;
    for (FiFoMode mode : FiFoMode.values()) {
      if (mode.getMask() == mask) {
        return mode;
      }
    }
    return FiFoMode.BYPASS;
  }

  public void setFifoMode(FiFoMode mode) throws IOException {
    setControlRegister(~FIFO_MODE, mode.getMask() << 5);
  }

  public int getFiFoWaterMark() throws IOException {
    return (registerValue & FIFO_THRESHOLD);
  }

  public void setFiFoWaterMark(int waterMark) throws IOException {
    setControlRegister(~FIFO_THRESHOLD, (waterMark & FIFO_THRESHOLD));
  }

}
