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

public class OffsetCompensationRegister extends SingleByteRegister {

  public OffsetCompensationRegister(I2CDevice sensor, int address, String name) throws IOException {
    super(sensor, address, name);
  }

  public int getOffset() throws IOException {
    reload();
    return registerValue & 0xFF;
  }

  public void setOffset(int offset) throws IOException {
    registerValue = (byte) ((registerValue & 0xFF00) | (offset & 0xFF));
    sensor.write(address, registerValue);
  }
}

