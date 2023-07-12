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

package io.mapsmessaging.devices.i2c.devices.sensors.lps25.registers;

import io.mapsmessaging.devices.i2c.devices.sensors.lps25.Lps25Sensor;

import java.io.IOException;
import java.util.concurrent.locks.LockSupport;

public class Control2 extends Register {

  private static final byte CONTROL_REGISTER2 = 0x21;

  public Control2(Lps25Sensor sensor) throws IOException {
    super(sensor, CONTROL_REGISTER2);
    reload();
  }

  public void boot() throws IOException {
    setControlRegister(0b01111111, 0b10000000);
    sensor.delay(50);
  }

  public void enableFiFo(boolean flag) throws IOException {
    int value = flag ? 0b01000000 : 0;
    setControlRegister(0b10111111, value);
  }

  public boolean isFiFoEnabled() throws IOException {
    return (registerValue & 0b01000000) != 0;
  }

  public void enableStopFiFoOnThreshold(boolean flag) throws IOException {
    int value = flag ? 0b00100000 : 0;
    setControlRegister(0b11011111, value);
  }

  public boolean isStopFiFoOnThresholdEnabled() throws IOException {
    return (registerValue & 0b00100000) != 0;
  }

  public void reset() throws IOException {
    setControlRegister(0b11111011, 0b100);
    int count = 0;
    boolean wait = true;
    while (wait & count < 100000) {
      try {
        wait = sensor.readRegister(super.address) > -1;
      } catch (IOException e) {
        // ignore
      }
      if (wait) {
        LockSupport.parkNanos(1000);
      }
      count++;
    }
  }

  public void enableOneShot(boolean flag) throws IOException {
    int value = flag ? 0b1 : 0;
    setControlRegister(0b11111110, value);
  }

  public boolean isOneShotEnabled() {
    return (registerValue & 0b1) != 0;
  }

}
