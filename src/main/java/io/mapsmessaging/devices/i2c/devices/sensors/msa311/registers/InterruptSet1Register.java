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

package io.mapsmessaging.devices.i2c.devices.sensors.msa311.registers;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.data.InterruptSet1Data;

import java.io.IOException;

public class InterruptSet1Register extends SingleByteRegister {

  private static final byte NEW_DATA_INT_EN = (byte) 0b10000;
  private static final byte FREEFALL_INT_EN = (byte) 0b01000;

  public InterruptSet1Register(I2CDevice sensor) throws IOException {
    super(sensor, 0x17, "Int_Set_1");
  }

  public boolean isNewDataInterruptEnabled() {
    return (registerValue & NEW_DATA_INT_EN) != 0;
  }

  public void setNewDataInterruptEnabled(boolean enabled) throws IOException {
    int value = enabled ? NEW_DATA_INT_EN : 0;
    setControlRegister(~NEW_DATA_INT_EN, value);
  }

  public boolean isFreefallInterruptEnabled() {
    return (registerValue & FREEFALL_INT_EN) != 0;
  }

  public void setFreefallInterruptEnabled(boolean enabled) throws IOException {
    int value = enabled ? FREEFALL_INT_EN : 0;
    setControlRegister(~FREEFALL_INT_EN, value);
  }

  @Override
  public RegisterData toData() throws IOException {
    return new InterruptSet1Data(
        isNewDataInterruptEnabled(),
        isFreefallInterruptEnabled()
    );
  }

  @Override
  public boolean fromData(RegisterData input) throws IOException {
    if (input instanceof InterruptSet1Data data) {
      setNewDataInterruptEnabled(data.isNewDataInterruptEnabled());
      setFreefallInterruptEnabled(data.isFreefallInterruptEnabled());
      return true;
    }
    return false;
  }

}

