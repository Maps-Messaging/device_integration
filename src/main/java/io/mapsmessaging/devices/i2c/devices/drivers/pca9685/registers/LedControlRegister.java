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

package io.mapsmessaging.devices.i2c.devices.drivers.pca9685.registers;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.MultiByteRegister;
import io.mapsmessaging.devices.i2c.devices.drivers.pca9685.data.LedControlData;

import java.io.IOException;
import java.util.Arrays;

public class LedControlRegister extends MultiByteRegister {

  private static final int FULL_ON = 0b00010000;
  private static final int FULL_OFF = 0b00010000;

  public LedControlRegister(I2CDevice sensor, int address, String name) throws IOException {
    super(sensor, address, 4, name);
    reload();
  }

  public boolean isFullOn() {
    return (buffer[1] & FULL_ON) != 0;
  }

  public void setFullOn(boolean flag) throws IOException {
    setFullFlag(1, ~FULL_ON, flag ? FULL_ON : 0);
  }

  public boolean isFullOff() {
    return (buffer[3] & FULL_OFF) != 0;
  }

  public void setFullOff(boolean flag) throws IOException {
    setFullFlag(3, ~FULL_OFF, flag ? FULL_OFF : 0);
  }

  public int getOn() {
    return readVal(0);
  }

  public void reset() throws IOException {
    Arrays.fill(buffer, (byte) 0);
    sensor.write(address, buffer);
  }

  public void setRate(int on, int off) throws IOException {
    writeVal(on, off);
  }

  public int getOff() {
    return readVal(2);
  }

  @Override
  public boolean fromData(RegisterData input) throws IOException {
    if (input instanceof LedControlData) {
      LedControlData data = (LedControlData) input;
      setRate(data.getOn(), data.getOff());
      return true;
    }
    return false;
  }

  @Override
  public RegisterData toData() throws IOException {
    return new LedControlData(getOn(), getOff());
  }

  protected void setFullFlag(int offset, int mask, int value) throws IOException {
    buffer[offset] = (byte) ((buffer[offset] & mask) | value);
    sensor.write(address + offset, buffer[offset]);
  }


  protected void writeVal(int on, int off) throws IOException {
    buffer[0] = (byte) (on & 0xff);
    buffer[1] = (byte) ((on >> 8) & 0b00001111);
    buffer[2] = (byte) (off & 0xff);
    buffer[3] = (byte) ((off >> 8) & 0b00001111);
    sensor.write(address, buffer);
  }

  protected int readVal(int offset) {
    return buffer[offset] & 0xff | ((buffer[offset + 1] & 0xf) << 8);
  }
}
