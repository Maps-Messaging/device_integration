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

package io.mapsmessaging.devices.i2c.devices.sensors.lps25.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.MultiByteRegister;

import java.io.IOException;

public class PressureRegister extends MultiByteRegister {

  public PressureRegister(I2CDevice sensor) {
    super(sensor, 0x28 | 0x80, 3, "PRESS_OUT");
  }

  @Override
  public int getAddress() {
    return address & (~0x80);
  }

  public float getPressure() throws IOException {
    reload();
    int raw = asInt();
    if ((raw & 0x00800000) != 0) {
      raw |= 0xff000000;
    }
    float v = raw;
    return v / 4096.0f;
  }
}
