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

package io.mapsmessaging.devices.i2c.devices.sensors.ds3231.register;

import com.pi4j.io.i2c.I2C;

public class StatusRegister {

  private final I2C device;
  private byte statusByte;

  public StatusRegister(I2C device, byte statusByte) {
    this.statusByte = statusByte;
    this.device = device;

  }

  public boolean isOscillatorStopped() {
    return (statusByte & 0x80) != 0;
  }

  public boolean is32kHzOutputEnabled() {
    return (statusByte & 0x08) != 0;
  }

  public boolean isAlarm2FlagSet() {
    return (statusByte & 0x02) != 0;
  }

  public boolean isAlarm1FlagSet() {
    return (statusByte & 0x01) != 0;
  }

  public void clearAlarm2Flag() {
    statusByte &= 0xFD;
    device.writeRegister(0xf, statusByte);
  }

  public void clearAlarm1Flag() {
    statusByte &= 0xFE;
    device.writeRegister(0xf, statusByte);
  }

  public byte toByte() {
    return statusByte;
  }

  @Override
  public String toString() {
    return "Oscillator Stopped : " + isOscillatorStopped() + "\n" +
        "32kHz Output Enabled : " + is32kHzOutputEnabled() + "\n" +
        "Alarm 2 Flag Set : " + isAlarm2FlagSet() + "\n" +
        "Alarm 1 Flag Set : " + isAlarm1FlagSet() + "\n";
  }
}

