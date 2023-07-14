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

package io.mapsmessaging.devices.i2c.devices.sensors.msa311.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.Register;

import java.io.IOException;

public class ActiveDurRegister extends Register {

  private static final byte DURATION_MASK = 0b00000011;

  public ActiveDurRegister(I2CDevice sensor) {
    super(sensor, 0x27);
  }

  public int getDuration() throws IOException {
    reload();
    return (registerValue & DURATION_MASK) + 1;
  }

  public void setDuration(int duration) throws IOException {
    int value = (registerValue & ~DURATION_MASK) | ((duration - 1) & DURATION_MASK);
    sensor.write(address, (byte) value);
  }
}
