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
import io.mapsmessaging.devices.i2c.devices.sensors.lps25.data.ThresholdPressureData;

import java.io.IOException;

public class ThresholdPressureRegister extends MultiByteRegister {

  public ThresholdPressureRegister(I2CDevice sensor) {
    super(sensor, 0x30 | 0x80, 2, "THS_P");
  }

  @Override
  public int getAddress() {
    return address & (~0x80);
  }

  public float getThreshold() {
    return asInt() / 16.0f;
  }

  public void setThreshold(float value) throws IOException {
    int val = Math.round(value * 16);
    write(val);
  }

  public RegisterData toData() throws IOException {
    return new ThresholdPressureData(getThreshold());
  }

  public boolean fromData(RegisterData input) throws IOException {
    if (input instanceof ThresholdPressureData) {
      ThresholdPressureData data = (ThresholdPressureData) input;
      setThreshold(data.getThreshold());
      return true;
    }
    return false;
  }

}
