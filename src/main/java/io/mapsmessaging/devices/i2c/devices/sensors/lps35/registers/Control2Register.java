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

package io.mapsmessaging.devices.i2c.devices.sensors.lps35.registers;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.lps35.data.Control2Data;

import java.io.IOException;

public class Control2Register extends SingleByteRegister {

  private static final int BOOT = 0b10000000;
  private static final int FIFO_ENABLED = 0b01000000;
  private static final int FIFO_STOP_ON_THRESHOLD = 0b00100000;
  private static final int SOFT_RESET = 0b00000100;
  private static final int ONE_SHOT = 0b00000001;

  public Control2Register(I2CDevice sensor) throws IOException {
    super(sensor, 0x11, "CTRL_REG2");
    reload();
  }

  public void boot() throws IOException {
    setControlRegister(~BOOT, BOOT);
    sensor.delay(50);
  }

  public void enableFiFo(boolean flag) throws IOException {
    setControlRegister(~FIFO_ENABLED, flag ? FIFO_ENABLED : 0);
  }

  public boolean isFiFoEnabled() {
    return (registerValue & FIFO_ENABLED) != 0;
  }

  public void enableStopFiFoOnThreshold(boolean flag) throws IOException {
    setControlRegister(~FIFO_STOP_ON_THRESHOLD, flag ? FIFO_STOP_ON_THRESHOLD : 0);
  }

  public boolean isStopFiFoOnThresholdEnabled() {
    return (registerValue & FIFO_STOP_ON_THRESHOLD) != 0;
  }

  public void softReset() throws IOException {
    setControlRegister(~SOFT_RESET, SOFT_RESET);
    sensor.delay(50);
  }

  public void enableOneShot(boolean flag) throws IOException {
    setControlRegister(~ONE_SHOT, flag ? ONE_SHOT : 0);
  }

  public boolean isOneShotEnabled() {
    return (registerValue & ONE_SHOT) != 0;
  }

  @Override
  public RegisterData toData() throws IOException {
    boolean fiFoEnabled = isFiFoEnabled();
    boolean stopFiFoOnThresholdEnabled = isStopFiFoOnThresholdEnabled();
    boolean oneShotEnabled = isOneShotEnabled();
    return new Control2Data(fiFoEnabled, stopFiFoOnThresholdEnabled, oneShotEnabled);
  }

  // Method to set Control2Register data from Control2Data
  @Override
  public boolean fromData(RegisterData input) throws IOException {
    if (input instanceof Control2Data data) {
      enableFiFo(data.isFiFoEnabled());
      enableStopFiFoOnThreshold(data.isStopFiFoOnThresholdEnabled());
      enableOneShot(data.isOneShotEnabled());
      return true;
    }
    return false;
  }

}