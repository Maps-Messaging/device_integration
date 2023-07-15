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

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.values.Sensitivity;

import java.io.IOException;

public class ActiveThRegister extends SingleByteRegister {

  public ActiveThRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0x28);
  }

  public double getThreshold(Sensitivity sensitivity) throws IOException {
    reload();
    int value = registerValue & 0xFF;
    double sensitivityFactor = sensitivity.getFactor();
    return value * sensitivityFactor;
  }

  public void setThreshold(double threshold, Sensitivity sensitivity) throws IOException {
    double sensitivityFactor = sensitivity.getFactor();
    int value = (int) Math.round(threshold / sensitivityFactor);
    sensor.write(address, (byte) value);
  }

}
