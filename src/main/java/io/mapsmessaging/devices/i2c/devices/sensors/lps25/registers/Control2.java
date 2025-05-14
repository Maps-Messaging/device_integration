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

package io.mapsmessaging.devices.i2c.devices.sensors.lps25.registers;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.lps25.data.Control2Data;

import java.io.IOException;

public class Control2 extends SingleByteRegister {

  private static final byte CONTROL_REGISTER2 = 0x21;

  private static final byte BOOT = (byte) 0b10000000;
  private static final byte FIFO_ENABLED = 0b01000000;
  private static final byte STOP_ON_FIFO_THRESHOLD = 0b00100000;
  private static final byte FIFO_MEAN_DEC = 0b10000;
  private static final byte RESET = 0b00000100;
  private static final byte ENABLE_AUTO_ZERO = 0b00000010;
  private static final byte ENABLE_ONE_SHOT = 0b00000001;


  public Control2(I2CDevice sensor) throws IOException {
    super(sensor, CONTROL_REGISTER2, "CTRL_REG2");
    reload();
  }

  public void boot() throws IOException {
    setControlRegister(~BOOT, BOOT);
    waitForDevice();
  }

  public void enableFiFo(boolean flag) throws IOException {
    int value = flag ? FIFO_ENABLED : 0;
    setControlRegister(~FIFO_ENABLED, value);
  }

  public boolean isFiFoEnabled() {
    return (registerValue & FIFO_ENABLED) != 0;
  }


  public void setFiFoMeanDec(boolean flag) throws IOException {
    int value = flag ? FIFO_MEAN_DEC : 0;
    setControlRegister(~FIFO_MEAN_DEC, value);
  }

  public boolean isFiFoMeanDecEnabled() {
    return (registerValue & FIFO_MEAN_DEC) != 0;
  }


  public void enableStopFiFoOnThreshold(boolean flag) throws IOException {
    int value = flag ? STOP_ON_FIFO_THRESHOLD : 0;
    setControlRegister(~STOP_ON_FIFO_THRESHOLD, value);
  }

  public boolean isStopFiFoOnThresholdEnabled() {
    return (registerValue & STOP_ON_FIFO_THRESHOLD) != 0;
  }

  public void reset() throws IOException {
    setControlRegister(~RESET, RESET);
    waitForDevice();
  }

  public void enableAutoZero(boolean flag) throws IOException {
    int value = flag ? ENABLE_AUTO_ZERO : 0;
    setControlRegister(~ENABLE_AUTO_ZERO, value);
  }

  public boolean isAutoZeroEnabled() {
    return (registerValue & ENABLE_AUTO_ZERO) != 0;
  }

  public void enableOneShot(boolean flag) throws IOException {
    int value = flag ? ENABLE_ONE_SHOT : 0;
    setControlRegister(~ENABLE_ONE_SHOT, value);
  }

  public boolean isOneShotEnabled() {
    return (registerValue & ENABLE_ONE_SHOT) != 0;
  }

  @Override
  public RegisterData toData() {
    return new Control2Data(isFiFoEnabled(), isStopFiFoOnThresholdEnabled(), isAutoZeroEnabled(), isOneShotEnabled());
  }

  @Override
  public boolean fromData(RegisterData input) throws IOException {
    if (input instanceof Control2Data) {
      Control2Data data = (Control2Data) input;
      enableFiFo(data.isFifoEnabled());
      enableStopFiFoOnThreshold(data.isStopFifoOnThreshold());
      enableAutoZero(data.isAutoZeroEnabled());
      enableOneShot(data.isOneShotEnabled());
      return true;
    }
    return false;
  }

}
