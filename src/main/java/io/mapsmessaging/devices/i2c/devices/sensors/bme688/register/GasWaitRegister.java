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

package io.mapsmessaging.devices.i2c.devices.sensors.bme688.register;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.data.GasWait;

import java.io.IOException;

public class GasWaitRegister extends SingleByteRegister {

  private static final int[] MULTIPLICATION_TABLE = {1, 4, 16, 64};

  public GasWaitRegister(I2CDevice sensor, int address, String name) throws IOException {
    super(sensor, address, name);
    reload();
  }

  public int getTimerSteps() {
    return registerValue & 0b111111;
  }

  public void setTimerSteps(int value) {
    registerValue = (byte) ((registerValue & 0b11000000) | (value & 0b111111));

  }

  public int getMultiplicationFactor() {
    return MULTIPLICATION_TABLE[(registerValue & 0b11000000) >> 6];
  }

  public void setMultiplicationFactor(int value) {
    int idx = 0;
    boolean found = false;
    for (int val : MULTIPLICATION_TABLE) {
      if (val == value) {
        found = true;
        break;
      }
      idx++;
    }
    if (!found) {
      idx = 0;
    }
    registerValue = (byte) ((registerValue & 0b00111111) | (idx & 0b11) << 6);
  }

  @Override
  public RegisterData toData() throws IOException {
    return new GasWait(getMultiplicationFactor(), getTimerSteps());
  }

  public void updateRegister() throws IOException {
    sensor.write(address, registerValue);
  }
}