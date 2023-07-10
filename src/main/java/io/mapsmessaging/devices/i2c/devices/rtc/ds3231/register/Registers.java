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
import lombok.Getter;
import lombok.ToString;

@ToString
public class Registers {

  private final I2C device;
  private byte[] registerValues;
  @Getter
  private ControlRegister controlRegister;
  @Getter
  private StatusRegister statusRegister;
  @Getter
  private AlarmRegister alarm1;
  @Getter
  private AlarmRegister alarm2;

  public Registers(I2C device) {
    this.device = device;
  }

  protected static int bcdToDecimal(int bcdValue) {
    return ((bcdValue & 0xF0) >> 4) * 10 + (bcdValue & 0x0F);
  }

  protected static int convert12HourTo24Hour(int hours, boolean isPM) {
    if (isPM) {
      hours += 12;
    }
    return hours;
  }

  protected static byte decimalToBcd(int decimalValue) {
    return (byte) (((decimalValue / 10) << 4) | (decimalValue % 10));
  }

  public void setRegisterValues(byte[] values) {
    registerValues = values;
    controlRegister = new ControlRegister(device, registerValues[0xE]);
    statusRegister = new StatusRegister(device, registerValues[0xf]);
    byte[] alarm1Register = new byte[4];
    System.arraycopy(values, 7, alarm1Register, 0, 4);
    alarm1 = new AlarmRegister(alarm1Register, true, device, 7);

    byte[] alarm2Register = new byte[3];
    System.arraycopy(values, 11, alarm2Register, 0, 3);
    alarm2 = new AlarmRegister(alarm2Register, false, device, 11);
  }

  public int getSeconds() {
    return bcdToDecimal(registerValues[0] & 0x7F);
  }

  public void setSeconds(int seconds) {
    registerValues[0] = decimalToBcd(seconds % 60);
    device.writeRegister(0, registerValues[0]);
  }

  public int getMinutes() {
    return bcdToDecimal(registerValues[1] & 0x7F);
  }

  public void setMinutes(int minutes) {
    registerValues[1] = decimalToBcd(minutes % 60);
    device.writeRegister(1, registerValues[1]);
  }

  public int getHours() {
    byte value = registerValues[2];
    boolean is12HourFormat = (value & 0x40) != 0;
    int hours = bcdToDecimal(value & 0x3F);

    if (is12HourFormat) {
      boolean isPM = (value & 0x20) != 0;
      hours = convert12HourTo24Hour(hours, isPM);
    }
    return hours % 24;
  }

  public void setHours(int hours, boolean is12HourFormat) {
    if (is12HourFormat) {
      boolean isPM = hours >= 12;
      if (hours > 12) {
        hours -= 12;
      }
      registerValues[2] = (byte) (0x40 | (decimalToBcd(hours) & 0x1F));
      if (isPM) {
        registerValues[2] |= 0x20;
      }
    } else {
      registerValues[2] = decimalToBcd(hours % 24);
    }
    device.writeRegister(2, registerValues[2]);
  }


  public int getDayOfWeek() {
    return registerValues[3] & 0b111;
  }

  public void setDayOfWeek(int dayOfWeek) {
    registerValues[3] = (byte) (dayOfWeek & 0x07);
    device.writeRegister(3, registerValues[3]);
  }

  public int getDate() {
    return bcdToDecimal(registerValues[4] & 0x3F);
  }

  public void setDate(int date) {
    registerValues[4] = decimalToBcd(date % 32);
    device.writeRegister(4, registerValues[4]);
  }

  public int getMonth() {
    return bcdToDecimal(registerValues[5] & 0x1F);
  }

  public void setMonth(int month) {
    registerValues[5] = decimalToBcd(month % 13);
    device.writeRegister(5, registerValues[5]);
  }

  public int getYear() {
    return bcdToDecimal(registerValues[6]) + 2000;
  }

  public void setYear(int year) {
    registerValues[6] = decimalToBcd((year - 2000) % 100);
    device.writeRegister(6, registerValues[6]);
  }

  public int getControl() {
    return registerValues[14] & 0xFF;
  }

  public void setControl(int control) {
    registerValues[14] = (byte) (control & 0xFF);
  }

  public int getStatus() {
    return registerValues[15] & 0xFF;
  }

  public void setStatus(int status) {
    registerValues[15] = (byte) (status & 0xFF);
  }

  public int getAgingOffset() {
    return registerValues[16];
  }

  public void setAgingOffset(int agingOffset) {
    registerValues[16] = (byte) agingOffset;
  }

  public float getTemperature() {
    int tempValue = ((registerValues[17] & 0x7F) << 2) + ((registerValues[18] >> 6) & 0x03);
    return tempValue / 4.0f;
  }

}
