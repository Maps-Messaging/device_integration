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

package io.mapsmessaging.devices.i2c.devices;

import io.mapsmessaging.devices.i2c.I2CDevice;
import lombok.Getter;

import java.io.IOException;

public abstract class Register {

  protected final I2CDevice sensor;

  @Getter
  protected final int address;

  @Getter
  protected final String name;


  protected Register(I2CDevice sensor, int address, String name) {
    this.address = address;
    this.sensor = sensor;
    this.name = name;
    sensor.getRegisterMap().addRegister(this);
  }

  protected abstract void reload() throws IOException;

  protected abstract void setControlRegister(int mask, int value) throws IOException;

  protected void waitForDevice() {
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

  public abstract String toString(int maxLength);

  protected String displayRegister(int length, int add, int val) {
    String paddedName = String.format("%-" + length + "s", getName());
    String paddedHex = String.format("0x%02X", (add & 0xff));
    String binaryString = String.format("%8s", Integer.toBinaryString(val & 0xFF)).replace(' ', '0');
    String paddedValHex = String.format("0x%02X", (val & 0xff));
    return paddedName + "\t" + paddedHex + "\t [" + binaryString+"] "+paddedValHex ;
  }

}
