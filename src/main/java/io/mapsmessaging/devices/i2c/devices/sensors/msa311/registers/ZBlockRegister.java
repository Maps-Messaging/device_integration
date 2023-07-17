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

public class ZBlockRegister extends SingleByteRegister {

  private static final byte Z_BLOCKING_MASK = (byte) 0b00001111;

  public ZBlockRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0x2D, "Z_Block");
  }

  public float getZBlockingThreshold() throws IOException {
    reload();
    int val = registerValue & Z_BLOCKING_MASK;
    return val * 0.0625f;
  }

  public void setZBlockingThreshold(float threshold) throws IOException {
    int val = Math.round(threshold / 0.0625f);
    registerValue = (byte) ((registerValue & ~Z_BLOCKING_MASK) | val);
    sensor.write(address, registerValue);
  }
}