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

package io.mapsmessaging.devices.i2c.devices.sensors.bme688.register;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.data.ControlGas1;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.values.HeaterStep;

import java.io.IOException;

public class ControlGas1Register extends SingleByteRegister {

  public ControlGas1Register(I2CDevice sensor) throws IOException {
    super(sensor, 0x71, "Ctrl_gas_1");
  }

  public boolean isRunGas() throws IOException {
    reload();
    return (registerValue & 0b100000) != 0;
  }

  public void setRunGas(boolean flag) throws IOException {
    setControlRegister(0b100000, flag ? 0b100000 : 0);
  }

  public HeaterStep getNbConv() throws IOException {
    reload();
    return HeaterStep.values()[registerValue & 0b1111];
  }

  public void setNbConv(HeaterStep heaterStep) throws IOException {
    setControlRegister(0b1111, heaterStep.getValue());
  }

  @Override
  public RegisterData toData() throws IOException {
    return new ControlGas1(isRunGas(), getNbConv());
  }
}