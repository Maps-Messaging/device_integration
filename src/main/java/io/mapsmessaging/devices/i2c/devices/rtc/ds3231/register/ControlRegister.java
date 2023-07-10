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

package io.mapsmessaging.devices.i2c.devices.rtc.ds3231.register;

import com.pi4j.io.i2c.I2C;

public class ControlRegister {
  private final I2C device;
  private byte controlByte;

  public ControlRegister(I2C device, byte controlByte) {
    this.controlByte = controlByte;
    this.device = device;
  }

  public boolean isOscillatorEnabled() {
    return (controlByte & 0x80) != 0;
  }

  public void setOscillatorEnabled(boolean enabled) {
    if (enabled) {
      controlByte |= 0x80;
    } else {
      controlByte &= 0x7F;
    }
    write();
  }

  public boolean isSquareWaveEnabled() {
    return (controlByte & 0x40) != 0;
  }

  public void setSquareWaveEnabled(boolean enabled) {
    if (enabled) {
      controlByte |= 0x40;
    } else {
      controlByte &= 0xBF;
    }
    write();
  }

  public boolean isConvertTemperatureEnabled() {
    return (controlByte & 0x20) != 0;
  }

  public void setConvertTemperature(boolean enabled) {
    if (enabled) {
      controlByte |= 0x20;
    } else {
      controlByte &= 0xDF;
    }
    write();
  }

  public int getSquareWaveFrequency() {
    int frequencyBits = (controlByte >> 3) & 0x03;
    switch (frequencyBits) {
      case 0:
        return 1; // 1Hz
      case 1:
        return 4096; // 4096Hz
      case 2:
        return 8192; // 8192Hz
      case 3:
        return 32768; // 32768Hz
      default:
        return 0; // Invalid frequency
    }
  }

  public void setSquareWaveFrequency(int frequency) {
    int frequencyBits;
    switch (frequency) {
      case 1:
        frequencyBits = 0;
        break;
      case 4096:
        frequencyBits = 1;
        break;
      case 8192:
        frequencyBits = 2;
        break;
      case 32768:
        frequencyBits = 3;
        break;
      default:
        frequencyBits = 0; // Invalid frequency, default to 1Hz
    }
    controlByte = (byte) ((controlByte & 0xC7) | (frequencyBits << 3));
    write();
  }

  public boolean isSquareWaveInterruptEnabled() {
    return (controlByte & 0x04) != 0;
  }

  public void setSquareWaveInterruptEnabled(boolean enabled) {
    if (enabled) {
      controlByte |= 0x04;
    } else {
      controlByte &= 0xFB;
    }
    write();
  }

  public boolean isAlarm1InterruptEnabled() {
    return (controlByte & 0x01) != 0;
  }

  public void setAlarm1InterruptEnabled(boolean enabled) {
    if (enabled) {
      controlByte |= 0x01;
    } else {
      controlByte &= 0xFE;
    }
    write();
  }

  public boolean isAlarm2InterruptEnabled() {
    return (controlByte & 0x02) != 0;
  }

  public void setAlarm2InterruptEnabled(boolean enabled) {
    if (enabled) {
      controlByte |= 0x02;
    } else {
      controlByte &= 0xFD;
    }
    write();
  }

  private void write() {
    device.writeRegister(0xE, controlByte);
  }

  @Override
  public String toString() {
    return "Oscillator : " + isOscillatorEnabled() + "\n" +
        "Battery Backed Square Wave : " + isSquareWaveEnabled() + "\n" +
        "Convert Temperature : " + isConvertTemperatureEnabled() + "\n" +
        "Square Wave Frequency : " + getSquareWaveFrequency() + "\n" +
        "Interrupt Enabled : " + isSquareWaveInterruptEnabled() + "\n" +
        "Alarm 2 Enabled : " + isAlarm1InterruptEnabled() + "\n" +
        "Alarm 1 Enabled : " + isAlarm2InterruptEnabled() + "\n";
  }

}

