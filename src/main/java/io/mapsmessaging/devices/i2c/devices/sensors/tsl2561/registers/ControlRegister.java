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

package io.mapsmessaging.devices.i2c.devices.sensors.tsl2561.registers;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.tsl2561.data.ControlData;

import java.io.IOException;

public class ControlRegister extends SingleByteRegister {

  private static final byte POWER_MASK = 0b00000011;

  public ControlRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0x80, "Control");
  }

  public void powerOn() throws IOException {
    setControlRegister(~POWER_MASK, 0b11);
    sensor.delay(500);
  }


  public void powerOff() throws IOException {
    setControlRegister(~POWER_MASK, 0b0);
  }

  @Override
  public RegisterData toData() throws IOException {
    boolean powerOn = (registerValue & POWER_MASK) == 0b11;
    return new ControlData(powerOn);
  }

  // Method to set ControlRegister data from ControlData
  @Override
  public boolean fromData(RegisterData input) throws IOException {
    if (input instanceof ControlData) {
      ControlData data = (ControlData) input;
      int value = data.isPowerOn() ? 0b11 : 0b00;
      setControlRegister(~POWER_MASK, value);
      return true;
    }
    return false;
  }

}
