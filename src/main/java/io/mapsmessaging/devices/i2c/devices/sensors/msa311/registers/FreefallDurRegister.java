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
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.data.FreefallDurData;

import java.io.IOException;

public class FreefallDurRegister extends SingleByteRegister {

  public FreefallDurRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0x22, "Freefall_Dur");
  }

  public int getFreefallDuration() throws IOException {
    reload();
    return (registerValue & 0xFF) + 1;
  }

  public void setFreefallDuration(int duration) throws IOException {
    duration = Math.max(2, Math.min(512, duration));
    registerValue = (byte) (duration - 1);
    sensor.write(address, registerValue);
  }

  @Override
  public RegisterData toData() throws IOException {
    return new FreefallDurData(getFreefallDuration());
  }

  @Override
  public boolean fromData(RegisterData input) throws IOException {
    if (input instanceof FreefallDurData) {
      FreefallDurData data = (FreefallDurData) input;
      setFreefallDuration(data.getFreefallDuration());
      return true;
    }
    return false;
  }

}
