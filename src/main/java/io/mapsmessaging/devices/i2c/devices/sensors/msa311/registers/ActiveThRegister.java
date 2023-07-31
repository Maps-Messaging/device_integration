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
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.data.ActiveThData;

import java.io.IOException;

public class ActiveThRegister extends SingleByteRegister {

  private final RangeRegister rangeRegister;

  public ActiveThRegister(I2CDevice sensor, RangeRegister rangeRegister) throws IOException {
    super(sensor, 0x28, "Active_Th");
    this.rangeRegister = rangeRegister;
  }

  public double getThreshold() throws IOException {
    reload();
    int value = registerValue & 0xFF;
    return value * rangeRegister.getRange().getThresholdMultiplier();
  }

  public void setThreshold(double threshold) throws IOException {
    double sensitivityFactor = rangeRegister.getRange().getThresholdMultiplier();
    int value = (int) Math.round(threshold / sensitivityFactor);
    sensor.write(address, (byte) value);
  }

  @Override
  public RegisterData toData() throws IOException {
    return new ActiveThData(getThreshold());
  }

  @Override
  public boolean fromData(RegisterData input) throws IOException {
    if (input instanceof ActiveThData) {
      ActiveThData data = (ActiveThData) input;
      setThreshold(data.getThreshold());
      return true;
    }
    return false;
  }

}
