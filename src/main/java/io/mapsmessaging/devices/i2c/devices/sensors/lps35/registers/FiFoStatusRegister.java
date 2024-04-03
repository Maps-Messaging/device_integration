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

package io.mapsmessaging.devices.i2c.devices.sensors.lps35.registers;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.lps35.data.FiFoStatusData35;

import java.io.IOException;

public class FiFoStatusRegister extends SingleByteRegister {

  public FiFoStatusRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0x26, "FIFO_STATUS");
  }

  public boolean hasHitThreshold() {
    return (registerValue & 0b10000000) != 0;
  }

  public boolean isOverwritten() {
    return (registerValue & 0b01000000) != 0;
  }

  public int getSize() {
    return (registerValue & 0b00011111) & 0xff;
  }

  @Override
  public RegisterData toData() {
    return new FiFoStatusData35(hasHitThreshold(), isOverwritten(), getSize());
  }


}
