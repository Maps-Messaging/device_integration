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
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;

import java.io.IOException;

public class FreefallThRegister extends SingleByteRegister {

  public FreefallThRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0x23);
  }

  public float getFreefallThreshold() throws IOException {
    reload();
    return (registerValue & 0xFF) * 7.81F;
  }

  public void setFreefallThreshold(int threshold) throws IOException {
    threshold = (int) Math.max(0, Math.min(255, threshold / 7.81f));
    registerValue = (byte) threshold;
    sensor.write(address, registerValue);
  }
}
