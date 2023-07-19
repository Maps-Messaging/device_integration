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

package io.mapsmessaging.devices.i2c.devices.sensors.lps25.registers;

import io.mapsmessaging.devices.deviceinterfaces.AbstractRegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.MultiByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.lps25.data.PressureOffsetData;

import java.io.IOException;

public class PressureOffset extends MultiByteRegister {

  public PressureOffset(I2CDevice sensor) {
    super(sensor, 0x39 | 0x80, 2, "RPDS");
  }

  @Override
  public int getAddress(){
    return address & (~0x80);
  }

  public int getPressureOffset() throws IOException {
    reload();
    return asInt();
  }

  public void setPressureOffset(int val) throws IOException {
    super.write(val);
  }

  public AbstractRegisterData toData() throws IOException {
    PressureOffsetData data = new PressureOffsetData();
    data.setPressureOffset(getPressureOffset());
    return data;
  }

  public boolean fromData(AbstractRegisterData input) throws IOException {
    if(input instanceof PressureOffsetData) {
      PressureOffsetData data = (PressureOffsetData) input;
      setPressureOffset(data.getPressureOffset());
      return true;
    }
    return false;
  }

}