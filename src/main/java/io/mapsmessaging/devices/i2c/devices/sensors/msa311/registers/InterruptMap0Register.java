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

import io.mapsmessaging.devices.deviceinterfaces.AbstractRegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.data.InterruptMap0Data;

import java.io.IOException;

public class InterruptMap0Register extends SingleByteRegister {

  private static final byte INT1_ORIENT = (byte) 0b01000000;
  private static final byte INT1_S_TAP = (byte) 0b00100000;
  private static final byte INT1_D_TAP = (byte) 0b00010000;
  private static final byte INT1_ACTIVE = (byte) 0b00000100;
  private static final byte INT1_FREEFALL = (byte) 0b00000001;

  public InterruptMap0Register(I2CDevice sensor) throws IOException {
    super(sensor, 0x19, "Int_Map_0");
  }

  public void mapOrientationInterruptToInt1(boolean enable) throws IOException {
    int value = enable ? INT1_ORIENT : 0;
    setControlRegister(~INT1_ORIENT, value);
  }

  public boolean isOrientationInterruptMappedToInt1() {
    return (registerValue & INT1_ORIENT) != 0;
  }

  public void mapSingleTapInterruptToInt1(boolean enable) throws IOException {
    int value = enable ? INT1_S_TAP : 0;
    setControlRegister(~INT1_S_TAP, value);
  }

  public boolean isSingleTapInterruptMappedToInt1() {
    return (registerValue & INT1_S_TAP) != 0;
  }

  public void mapDoubleTapInterruptToInt1(boolean enable) throws IOException {
    int value = enable ? INT1_D_TAP : 0;
    setControlRegister(~INT1_D_TAP, value);
  }

  public boolean isDoubleTapInterruptMappedToInt1() {
    return (registerValue & INT1_D_TAP) != 0;
  }

  public void mapActiveInterruptToInt1(boolean enable) throws IOException {
    int value = enable ? INT1_ACTIVE : 0;
    setControlRegister(~INT1_ACTIVE, value);
  }

  public boolean isActiveInterruptMappedToInt1() {
    return (registerValue & INT1_ACTIVE) != 0;
  }

  public void mapFreefallInterruptToInt1(boolean enable) throws IOException {
    int value = enable ? INT1_FREEFALL : 0;
    setControlRegister(~INT1_FREEFALL, value);
  }

  public boolean isFreefallInterruptMappedToInt1() {
    return (registerValue & INT1_FREEFALL) != 0;
  }
  @Override
  public AbstractRegisterData toData() throws IOException {
    return new InterruptMap0Data(
        isOrientationInterruptMappedToInt1(),
        isSingleTapInterruptMappedToInt1(),
        isDoubleTapInterruptMappedToInt1(),
        isActiveInterruptMappedToInt1(),
        isFreefallInterruptMappedToInt1()
    );
  }

  @Override
  public boolean fromData(AbstractRegisterData input) throws IOException {
    if(input instanceof InterruptMap0Data) {
      InterruptMap0Data data = (InterruptMap0Data) input;
      mapOrientationInterruptToInt1(data.isOrientationInterruptMappedToInt1());
      mapSingleTapInterruptToInt1(data.isSingleTapInterruptMappedToInt1());
      mapDoubleTapInterruptToInt1(data.isDoubleTapInterruptMappedToInt1());
      mapActiveInterruptToInt1(data.isActiveInterruptMappedToInt1());
      mapFreefallInterruptToInt1(data.isFreefallInterruptMappedToInt1());
      return true;
    }
    return false;
  }

}

