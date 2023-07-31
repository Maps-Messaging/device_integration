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

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.data.InterruptMap1Data;

import java.io.IOException;

public class InterruptMap1Register extends SingleByteRegister {

  private static final byte INT1_NEW_DATA = (byte) 0b00000001;

  public InterruptMap1Register(I2CDevice sensor) throws IOException {
    super(sensor, 0x1A, "Int_Map_1");
  }

  public void mapNewDataInterruptToInt1(boolean enable) throws IOException {
    int value = enable ? INT1_NEW_DATA : 0;
    setControlRegister(~INT1_NEW_DATA, value);
  }

  public boolean isNewDataInterruptMappedToInt1() {
    return (registerValue & INT1_NEW_DATA) != 0;
  }

  @Override
  public RegisterData toData() {
    InterruptMap1Data data = new InterruptMap1Data();
    data.setNewDataInterruptMappedToInt1(isNewDataInterruptMappedToInt1());
    return data;
  }

  @Override
  public boolean fromData(RegisterData input) throws IOException {
    if (input instanceof InterruptMap1Data) {
      InterruptMap1Data data = (InterruptMap1Data) input;
      mapNewDataInterruptToInt1(data.isNewDataInterruptMappedToInt1());
      return true;
    }
    return false;
  }

}
