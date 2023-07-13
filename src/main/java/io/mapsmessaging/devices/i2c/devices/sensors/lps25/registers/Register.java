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

import io.mapsmessaging.devices.i2c.I2CDevice;

import java.io.IOException;

public class Register {

  protected final I2CDevice sensor;
  protected final int address;
  protected byte registerValue;

  protected Register(I2CDevice sensor, int address) {
    this.address = address;
    this.sensor = sensor;
  }

  protected void reload() throws IOException {
    registerValue = (byte) (sensor.readRegister(address) & 0Xff);
  }

  protected void setControlRegister(int mask, int value) throws IOException {
    registerValue = (byte) ((registerValue & mask) | value);
    sensor.write(address, registerValue);
  }

  protected void waitForDevice(){
    int count = 0;
    boolean wait = true;
    while (wait & count < 10) {
      try {
        wait = sensor.readRegister(address) > -1;
      } catch (IOException e) {
        // ignore
      }
      if (wait) {
        sensor.delay(1);
      }
      count++;
    }
  }
}
