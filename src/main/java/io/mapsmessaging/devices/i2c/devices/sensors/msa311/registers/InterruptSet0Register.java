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
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.data.InterruptSet0Data;

import java.io.IOException;

public class InterruptSet0Register extends SingleByteRegister {

  private static final byte ORIENT_INT_EN = (byte) 0b01000000;
  private static final byte S_TAP_INT_EN = (byte) 0b00100000;
  private static final byte D_TAP_INT_EN = (byte) 0b00010000;
  private static final byte ACTIVE_INT_EN_Z = (byte) 0b00000100;
  private static final byte ACTIVE_INT_EN_Y = (byte) 0b00000010;
  private static final byte ACTIVE_INT_EN_X = (byte) 0b00000001;

  public InterruptSet0Register(I2CDevice sensor) throws IOException {
    super(sensor, 0x16, "Int_Set_0");
  }

  public boolean isOrientInterruptEnabled() {
    return (registerValue & ORIENT_INT_EN) != 0;
  }

  public void setOrientInterruptEnabled(boolean enabled) throws IOException {
    int value = enabled ? ORIENT_INT_EN : 0;
    setControlRegister(~ORIENT_INT_EN, value);
  }

  public boolean isSingleTapInterruptEnabled() {
    return (registerValue & S_TAP_INT_EN) != 0;
  }

  public void setSingleTapInterruptEnabled(boolean enabled) throws IOException {
    int value = enabled ? S_TAP_INT_EN : 0;
    setControlRegister(~S_TAP_INT_EN, value);
  }

  public boolean isDoubleTapInterruptEnabled() {
    return (registerValue & D_TAP_INT_EN) != 0;
  }

  public void setDoubleTapInterruptEnabled(boolean enabled) throws IOException {
    int value = enabled ? D_TAP_INT_EN : 0;
    setControlRegister(~D_TAP_INT_EN, value);
  }

  public boolean isActiveInterruptEnabledZ() {
    return (registerValue & ACTIVE_INT_EN_Z) != 0;
  }

  public void setActiveInterruptEnabledZ(boolean enabled) throws IOException {
    int value = enabled ? ACTIVE_INT_EN_Z : 0;
    setControlRegister(~ACTIVE_INT_EN_Z, value);
  }

  public boolean isActiveInterruptEnabledY() {
    return (registerValue & ACTIVE_INT_EN_Y) != 0;
  }

  public void setActiveInterruptEnabledY(boolean enabled) throws IOException {
    int value = enabled ? ACTIVE_INT_EN_Y : 0;
    setControlRegister(~ACTIVE_INT_EN_Y, value);
  }

  public boolean isActiveInterruptEnabledX() {
    return (registerValue & ACTIVE_INT_EN_X) != 0;
  }

  public void setActiveInterruptEnabledX(boolean enabled) throws IOException {
    int value = enabled ? ACTIVE_INT_EN_X : 0;
    setControlRegister(~ACTIVE_INT_EN_X, value);
  }

  @Override
  public RegisterData toData() throws IOException {
    return new InterruptSet0Data(
        isOrientInterruptEnabled(),
        isSingleTapInterruptEnabled(),
        isDoubleTapInterruptEnabled(),
        isActiveInterruptEnabledZ(),
        isActiveInterruptEnabledY(),
        isActiveInterruptEnabledX()
    );
  }

  @Override
  public boolean fromData(RegisterData input) throws IOException {
    if (input instanceof InterruptSet0Data data) {
      setOrientInterruptEnabled(data.isOrientInterruptEnabled());
      setSingleTapInterruptEnabled(data.isSingleTapInterruptEnabled());
      setDoubleTapInterruptEnabled(data.isDoubleTapInterruptEnabled());
      setActiveInterruptEnabledZ(data.isActiveInterruptEnabledZ());
      setActiveInterruptEnabledY(data.isActiveInterruptEnabledY());
      setActiveInterruptEnabledX(data.isActiveInterruptEnabledX());
      return true;
    }
    return false;
  }
}
