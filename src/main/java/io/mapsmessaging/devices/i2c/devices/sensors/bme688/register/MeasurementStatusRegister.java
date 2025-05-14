/*
 *
 *  Copyright [ 2020 - 2024 ] [Matthew Buckton]
 *  Copyright [ 2024 - 2025.  ] [Maps Messaging B.V.]
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package io.mapsmessaging.devices.i2c.devices.sensors.bme688.register;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;

import java.io.IOException;

public class MeasurementStatusRegister extends SingleByteRegister {

  public MeasurementStatusRegister(I2CDevice sensor, int index, String name) throws IOException {
    super(sensor, index, name);
  }

  public boolean hasNewData() {
    return (registerValue & 0b10000000) != 0;
  }

  public boolean isReadingGas() {
    return (registerValue & 0b01000000) != 0;
  }

  public boolean isMeasuring() {
    return (registerValue & 0b00100000) != 0;
  }

  public int getGasMeasureIndex() {
    return (registerValue & 0b1111);
  }

}