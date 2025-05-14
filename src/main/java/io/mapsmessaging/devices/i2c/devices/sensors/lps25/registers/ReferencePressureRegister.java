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

package io.mapsmessaging.devices.i2c.devices.sensors.lps25.registers;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.MultiByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.lps25.data.ReferencePressureData;

import java.io.IOException;

public class ReferencePressureRegister extends MultiByteRegister {


  public ReferencePressureRegister(I2CDevice sensor) {
    super(sensor, 0x08 | 0x80, 3, "REF_P");
  }

  @Override
  public int getAddress() {
    return address & (~0x80);
  }

  public int getReference() {
    return asInt();
  }

  public void setReference(int val) throws IOException {
    write(val);
  }

  public RegisterData toData() {
    ReferencePressureData data = new ReferencePressureData();
    data.setReference(getReference());
    return data;
  }

  public boolean fromData(RegisterData input) throws IOException {
    if (input instanceof ReferencePressureData) {
      ReferencePressureData data = (ReferencePressureData) input;
      setReference(data.getReference());
      return true;
    }
    return false;
  }

}
