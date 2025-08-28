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

package io.mapsmessaging.devices.i2c.devices.drivers.pca9685.registers;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.drivers.pca9685.data.SubAddressData;

import java.io.IOException;

public class SubAddressRegister extends SingleByteRegister {

  private static final int ADDRESS_MASK = 0b11111110;

  public SubAddressRegister(I2CDevice sensor, int address, String name) throws IOException {
    super(sensor, address, name);
  }

  public int getI2CAddress() throws IOException {
    reload();
    return registerValue >> 1;
  }

  public void setI2CAddress(int addr) throws IOException {
    int add = addr << 1;
    setControlRegister(ADDRESS_MASK, add);
  }

  @Override
  public boolean fromData(RegisterData input) throws IOException {
    if (input instanceof SubAddressData data) {
      setI2CAddress(data.getI2cAddress());
      return true;
    }
    return false;
  }

  @Override
  public RegisterData toData() throws IOException {
    return new SubAddressData(getI2CAddress());
  }
}
