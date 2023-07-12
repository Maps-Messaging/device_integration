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

public class Register {

  protected final Lps25Sensor sensor;
  protected final int address;
  protected byte registerValue;

  protected Register(Lps25Sensor sensor, int address) {
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
}
