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

package io.mapsmessaging.devices.i2c.devices.drivers.pca9685.registers;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.drivers.pca9685.data.PreScaleData;

import java.io.IOException;

public class PreScaleRegister extends SingleByteRegister {
  public PreScaleRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0xFE, "PRE_SCALE");
  }

  public void setPWMFrequency(float frequency) throws IOException {
    if (frequency < 40) {
      frequency = 40;
    }
    if (frequency > 1200) {
      frequency = 1200;
    }
    setPrescale(computePrescale(frequency));
  }

  public int getPrescale() throws IOException {
    reload();
    return registerValue;
  }

  public void setPrescale(int val) throws IOException {
    setControlRegister(0xff, val);
  }

  @Override
  public boolean fromData(RegisterData input) throws IOException {
    if (input instanceof PreScaleData) {
      PreScaleData data = (PreScaleData) input;
      setPrescale(data.getPrescale());
      return true;
    }
    return false;
  }

  @Override
  public RegisterData toData() throws IOException {
    return new PreScaleData(getPrescale());
  }

  protected int computePrescale(float frequency) {
    return (Math.round((25000000f / (4096f * frequency) - 0.5f)) & 0xff);
  }

}
