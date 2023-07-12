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
import io.mapsmessaging.devices.i2c.devices.sensors.lps25.values.DataRate;

import java.io.IOException;

public class Control1 extends Register {

  private static final byte CONTROL_REGISTER1 = 0x20;

  public Control1(Lps25Sensor sensor) throws IOException {
    super(sensor, CONTROL_REGISTER1);
    reload();
  }

  public void powerDown() throws IOException {
    setControlRegister((byte) 0b01111111, (byte) 0b10000000);
  }

  public DataRate getDataRate() throws IOException {
    int rateVal = (registerValue >> 4);
    for (DataRate rate : DataRate.values()) {
      if (rate.getMask() == rateVal) {
        return rate;
      }
    }
    return DataRate.RATE_ONE_SHOT;
  }

  public void setDataRate(DataRate rate) throws IOException {
    setControlRegister(0b0001111, (rate.getMask() << 4));
  }

  public boolean isInterruptGenerationEnabled() throws IOException {
    return (registerValue & 0b1000) != 0;
  }

  public void setInterruptGeneration(boolean flag) throws IOException {
    int value = flag ? 0b1000 : 0;
    setControlRegister(0b11110111, value);
  }

  public boolean isBlockUpdateSet() throws IOException {
    return (registerValue & 0b100) != 0;
  }

  public void setBlockUpdate(boolean flag) throws IOException {
    int value = flag ? 0b100 : 0;
    setControlRegister(0b11111011, value);
  }

  public void resetAutoZero(boolean flag) throws IOException {
    int value = flag ? 0b10 : 0;
    setControlRegister(0b11111101, value);
  }
}
