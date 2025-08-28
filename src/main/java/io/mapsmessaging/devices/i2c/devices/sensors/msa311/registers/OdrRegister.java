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
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.data.OdrData;
import io.mapsmessaging.devices.i2c.devices.sensors.msa311.values.Odr;

import java.io.IOException;

public class OdrRegister extends SingleByteRegister {

  private static final byte DISABLE_X_AXIS = (byte) 0b10000000;
  private static final byte DISABLE_Y_AXIS = 0b01000000;
  private static final byte DISABLE_Z_AXIS = 0b00100000;

  public OdrRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0x10, "ODR");
  }

  public Odr getOdr() throws IOException {
    reload();
    int val = registerValue & 0b1111;
    for (Odr odr : Odr.values()) {
      if (odr.getMask() == val) {
        return odr;
      }
    }
    return Odr.HERTZ_1000; // Default
  }

  public void setOdr(Odr odr) throws IOException {
    registerValue = (byte) ((registerValue & 0b11110000) | odr.getMask());
    sensor.write(address, registerValue);
  }

  public void disableXAxis(boolean flag) throws IOException {
    int value = flag ? DISABLE_X_AXIS : 0;
    setControlRegister(~DISABLE_X_AXIS, value);
  }

  public boolean isXAxisDisabled() {
    return (registerValue & DISABLE_X_AXIS) != 0;
  }

  public void disableYAxis(boolean flag) throws IOException {
    int value = flag ? DISABLE_Y_AXIS : 0;
    setControlRegister(~DISABLE_Y_AXIS, value);
  }

  public boolean isYAxisDisabled() {
    return (registerValue & DISABLE_Y_AXIS) != 0;
  }

  public void disableZAxis(boolean flag) throws IOException {
    int value = flag ? DISABLE_Z_AXIS : 0;
    setControlRegister(~DISABLE_Z_AXIS, value);
  }

  public boolean isZAxisDisabled() {
    return (registerValue & DISABLE_Z_AXIS) != 0;
  }

  @Override
  public RegisterData toData() throws IOException {
    return new OdrData(
        getOdr(),
        isXAxisDisabled(),
        isYAxisDisabled(),
        isZAxisDisabled()
    );
  }

  @Override
  public boolean fromData(RegisterData input) throws IOException {
    if (input instanceof OdrData data) {
      setOdr(data.getOdr());
      disableXAxis(data.isXAxisDisabled());
      disableYAxis(data.isYAxisDisabled());
      disableZAxis(data.isZAxisDisabled());
      return true;
    }
    return false;
  }

}
