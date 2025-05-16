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

package io.mapsmessaging.devices.i2c.devices.sensors.tsl2561.registers;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.tsl2561.data.InterruptControlData;
import io.mapsmessaging.devices.i2c.devices.sensors.tsl2561.values.InterruptControl;
import io.mapsmessaging.devices.i2c.devices.sensors.tsl2561.values.InterruptPersistence;

import java.io.IOException;

public class InterruptControlRegister extends SingleByteRegister {

  private static final byte INTR_MASK = 0b00110000;
  private static final byte PERSIST_MASK = 0b00001111;

  public InterruptControlRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0x86, "Interrupt Control");
    reload();
  }

  public InterruptControl getControl() {
    int val = (registerValue & INTR_MASK) >> 4;
    return InterruptControl.values()[val];
  }

  public void setControl(InterruptControl control) throws IOException {
    setControlRegister(~INTR_MASK, control.ordinal());
  }

  public InterruptPersistence getPersist() {
    int val = (registerValue & PERSIST_MASK);
    return InterruptPersistence.values()[val];
  }

  public void setPersist(InterruptPersistence persist) throws IOException {
    setControlRegister(~PERSIST_MASK, persist.ordinal());
  }

  @Override
  public RegisterData toData() throws IOException {
    InterruptControl control = getControl();
    InterruptPersistence persist = getPersist();
    return new InterruptControlData(control, persist);
  }

  // Method to set InterruptControlRegister data from InterruptControlData
  @Override
  public boolean fromData(RegisterData input) throws IOException {
    if (input instanceof InterruptControlData) {
      InterruptControlData data = (InterruptControlData) input;
      setControl(data.getControl());
      setPersist(data.getPersist());
      return true;
    }
    return false;
  }
}
