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

import io.mapsmessaging.devices.i2c.I2CDevice;
import lombok.Getter;
import lombok.ToString;

import java.io.IOException;
import java.time.LocalTime;

public class AlarmRegister {

  private final int secondIndex;
  private final int minuteIndex;
  private final int hourIndex;
  private final int dayDayIndex;

  private final byte[] registers;

  private final I2CDevice device;
  private final int addressOffset;

  @Getter
  private RATE rate;


  public AlarmRegister(byte[] alarmRegisters, boolean hasSeconds, I2CDevice device, int addressOffset) {
    this.device = device;
    this.addressOffset = addressOffset;
    this.registers = alarmRegisters;
    if (hasSeconds) {
      secondIndex = 0;
      minuteIndex = 1;
      hourIndex = 2;
      dayDayIndex = 3;
    } else {
      secondIndex = -1;
      minuteIndex = 0;
      hourIndex = 1;
      dayDayIndex = 2;
    }
    rate = computeRate();
  }

  private void write() throws IOException {
    device.write(addressOffset, registers);
  }

  protected byte[] getRegisters() {
    return registers;
  }

  private RATE computeRate() {
    int val = buildMaskFromBytes();
    boolean isDayOfWeek = isDayOfWeek();
    if (val == 0) {
      if (isDayOfWeek) {
        if (secondIndex < 0) {
          return RATE.DAY_HOURS_MINUTES_MATCH;
        } else {
          return RATE.DAY_HOURS_MINUTES_SECOND_MATCH;
        }
      } else {
        if (secondIndex < 0) {
          return RATE.DATE_HOURS_MINUTES_MATCH;
        }
      }
      return RATE.DATE_HOURS_MINUTES_SECOND_MATCH;
    }
    return RATE.findRate(val);
  }

  public void setRate(RATE rate) throws IOException {
    setBytesFromMask(rate.mask);
    this.rate = rate;
    write();
  }

  public LocalTime getTime() {
    int sec = getSeconds();
    int min = getMinutes();
    int hours = getHours();
    return LocalTime.of(hours, min, sec);
  }

  public void setTime(LocalTime localTime) throws IOException {
    setSeconds(localTime.getSecond());
    setMinutes(localTime.getMinute());
    setHours(localTime.getHour());
    write();
  }

  public int getDayOrDate() {
    return Registers.bcdToDecimal(registers[dayDayIndex] & 0x3F);
  }

  public void setDayOrDate(int dayOrDate) throws IOException {
    boolean isMask = (registers[dayDayIndex] & 0b10000000) != 0;
    if (rate.isDayOfWeek) {
      registers[dayDayIndex] = Registers.decimalToBcd(dayOrDate % 7);
      registers[dayDayIndex] |= 0b1000000;
    } else {
      registers[dayDayIndex] = Registers.decimalToBcd(dayOrDate % 32);
    }
    if (isMask) {
      registers[dayOrDate] |= 0b10000000;
    }
    write();
  }

  private int getSeconds() {
    if (secondIndex < 0) return 0;
    return Registers.bcdToDecimal(registers[secondIndex] & 0x7F);
  }

  private void setSeconds(int seconds) {
    if (secondIndex < 0) return;
    registers[secondIndex] = (byte) ((registers[secondIndex] & 0b10000000) | Registers.decimalToBcd(seconds % 60));
  }

  private int getMinutes() {
    return Registers.bcdToDecimal(registers[minuteIndex] & 0x7F);
  }

  private void setMinutes(int minutes) {
    registers[minuteIndex] = (byte) ((registers[minuteIndex] & 0b10000000) | Registers.decimalToBcd(minutes % 60));
  }

  private int getHours() {
    byte value = registers[hourIndex];
    boolean is12HourFormat = (value & 0x40) != 0;
    int hours = Registers.bcdToDecimal(value & 0x3F);

    if (is12HourFormat) {
      boolean isPM = (value & 0x20) != 0;
      hours = Registers.convert12HourTo24Hour(hours, isPM);
    }
    return hours;
  }

  private void setHours(int hours) {
    registers[hourIndex] = (byte) ((registers[hourIndex] & 0b10000000) | Registers.decimalToBcd(hours % 24));
  }

  private boolean isDayOfWeek() {
    return (registers[dayDayIndex] & 0b1000000) != 0;
  }


  private int buildMaskFromBytes() {
    int size = registers.length;
    int mask = 0;
    for (int i = 0; i < size; i++) {
      if ((registers[i] & 0x80) != 0) {
        mask |= (1 << (size - 1 - i));
      }
    }
    return mask;
  }

  private void setBytesFromMask(int mask) {
    int size = registers.length;
    for (int i = 0; i < size; i++) {
      if (((mask >> (size - 1 - i)) & 1) != 0) {
        registers[i] |= 0x80;
      } else {
        registers[i] &= 0x7F;
      }
    }
  }

  @Override
  public String toString() {
    if (rate.ignoreDayOrDate) {
      return getTime() + " " + rate;
    }
    return getDayOrDate() + " " + getTime() + " " + rate;
  }

  @ToString
  public enum RATE {
    UNKNOWN(0, false, false),

    ONCE_PER_SECOND(0b1111),
    SECONDS_MATCH(0b1110),
    MINUTES_SECONDS_MATCH(0b1100),
    HOURS_MINUTE_SECONDS_MATCH(0b1000),
    DATE_HOURS_MINUTES_SECOND_MATCH(0b0000, false),
    DAY_HOURS_MINUTES_SECOND_MATCH(0b0000, true),

    ONCE_PER_MINUTE(0b0111),
    MINUTES_MATCH(0b0110),
    HOURS_MINUTES_MATCH(0b0100),
    DATE_HOURS_MINUTES_MATCH(0b0000, false),
    DAY_HOURS_MINUTES_MATCH(0b0000, true);

    @Getter
    private final int mask;

    @Getter
    private final boolean isDayOfWeek;

    @Getter
    private final boolean ignoreDayOrDate;

    RATE(int mask) {
      this(mask, false, true);
    }

    RATE(int mask, boolean isDayOfWeek) {
      this(mask, isDayOfWeek, false);
    }

    RATE(int mask, boolean isDayOfWeek, boolean ignoreDayOrDate) {
      this.mask = mask;
      this.ignoreDayOrDate = ignoreDayOrDate;
      this.isDayOfWeek = isDayOfWeek;
    }

    public static RATE findRate(int mask) {
      for (RATE rate : RATE.values()) {
        if (rate.mask == mask) {
          return rate;
        }
      }
      return UNKNOWN;
    }

  }

}
