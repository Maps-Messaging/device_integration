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

package io.mapsmessaging.devices.i2c.devices.sensors.gravity.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.BufferedRegister;

public abstract class CrcValidatingRegsiter extends BufferedRegister {
  public CrcValidatingRegsiter(I2CDevice sensor, int address, String name, byte[] data) {
    super(sensor, address, name, data);

  }

  public CrcValidatingRegsiter(I2CDevice sensor, int address, int length, String name, byte[] data) {
    super(sensor, address, length, name, data);
  }


  protected byte calculateChecksum(byte[] data) {
    int checksum = 0;
    for (int i = 1; i < data.length - 2; i++) {
      int t = (data[i] & 0xff);
      checksum += t;
    }
    checksum = (~checksum) & 0xff;
    checksum = (checksum + 1);
    return (byte) checksum;
  }
}
