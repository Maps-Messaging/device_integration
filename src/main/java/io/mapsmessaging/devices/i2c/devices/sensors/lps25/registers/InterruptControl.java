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

package io.mapsmessaging.devices.i2c.devices.sensors.lps25.registers;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.lps25.data.InterruptControlData;

import java.io.IOException;

public class InterruptControl extends SingleByteRegister {

  private static final byte INTERRUPT_CONTROL = 0x24;
  private static final byte LATCH_INTERRUPT_ENABLE = 0b00000100;
  private static final byte LOW_INTERRUPT_ENABLE = 0b00000010;
  private static final byte HIGH_INTERRUPT_ENABLE = 0b00000001;


  public InterruptControl(I2CDevice sensor) throws IOException {
    super(sensor, INTERRUPT_CONTROL, "INTERRUPT_CFG");
    reload();
  }

  public void setLatchInterruptEnable(boolean flag) throws IOException {
    int value = flag ? LATCH_INTERRUPT_ENABLE : 0;
    setControlRegister(~LATCH_INTERRUPT_ENABLE, value);
  }

  public boolean isLatchInterruptEnabled() {
    return (registerValue & LATCH_INTERRUPT_ENABLE) != 0;
  }


  public void setInterruptOnLow(boolean flag) throws IOException {
    int value = flag ? LOW_INTERRUPT_ENABLE : 0;
    setControlRegister(~LOW_INTERRUPT_ENABLE, value);
  }

  public boolean isInterruptOnLowEnabled() {
    return (registerValue & LOW_INTERRUPT_ENABLE) != 0;
  }

  public void setInterruptOnHigh(boolean flag) throws IOException {
    int value = flag ? HIGH_INTERRUPT_ENABLE : 0;
    setControlRegister(~HIGH_INTERRUPT_ENABLE, value);
  }

  public boolean isInterruptOnHighEnabled() {
    return (registerValue & HIGH_INTERRUPT_ENABLE) != 0;
  }

  @Override
  public RegisterData toData() {
    InterruptControlData data = new InterruptControlData();
    data.setLatchInterruptEnabled(isLatchInterruptEnabled());
    data.setInterruptOnLowEnabled(isInterruptOnLowEnabled());
    data.setInterruptOnHighEnabled(isInterruptOnHighEnabled());
    return data;
  }

  @Override
  public boolean fromData(RegisterData input) throws IOException {
    if (input instanceof InterruptControlData) {
      InterruptControlData data = (InterruptControlData) input;
      setInterruptOnHigh(data.isInterruptOnHighEnabled());
      setInterruptOnLow(data.isInterruptOnLowEnabled());
      setLatchInterruptEnable(data.isLatchInterruptEnabled());
      return true;
    }
    return false;
  }

}