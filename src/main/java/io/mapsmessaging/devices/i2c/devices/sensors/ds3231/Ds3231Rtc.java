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

package io.mapsmessaging.devices.i2c.devices.sensors.ds3231;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.logging.Logger;
import io.mapsmessaging.logging.LoggerFactory;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Ds3231Rtc extends I2CDevice {

  private final Logger logger = LoggerFactory.getLogger(Ds3231Rtc.class);

  @Getter
  private final Ds3231Registers registers;

  public Ds3231Rtc(I2C device) {
    super(device);
    registers = new Ds3231Registers();
    read();
  }

  @Override
  public boolean isConnected() {
    return true;
  }

  public void read() {
    byte[] registerRead = new byte[19];
    for (int x = 0; x < registerRead.length; x++) {
      registerRead[x] = (byte) (readRegister(x) & 0xff);
    }
    registers.setRegisterValues(registerRead, device);
    System.err.println(registers);
  }

  public void write() {
    byte[] values = registers.getRegisterValues();
    for (int x = 0; x < values.length; x++) {
      write(x, values[x]);
    }
  }

  public LocalDateTime getDateTime() {
    return LocalDateTime.of(getDate(), getTime());
  }

  public void setDateTime(LocalDateTime dateTime) {
    setDate(dateTime.toLocalDate());
    setTime(dateTime.toLocalTime());
  }

  public LocalDate getDate() {
    return LocalDate.of(registers.getYear(), registers.getMonth(), registers.getDate());
  }

  public void setDate(LocalDate date) {
    boolean change = false;
    if (registers.getMonth() != date.getMonthValue()) {
      registers.setMonth(date.getMonthValue());
      change = true;
    }
    if (registers.getDate() != date.getDayOfMonth()) {
      registers.setDate(date.getDayOfMonth());
      change = true;
    }
    if (registers.getYear() != date.getYear()) {
      registers.setYear(date.getYear());
      change = true;
    }
    if (registers.getDayOfWeek() != date.getDayOfWeek().getValue()) {
      registers.setDayOfWeek(date.getDayOfWeek().getValue());
      change = true;
    }
    if (change) {
      write();
    }
  }

  public LocalTime getTime() {
    return LocalTime.of(registers.getHours(), registers.getMinutes(), registers.getSeconds());
  }

  public void setTime(LocalTime time) {
    boolean change = false;
    if (registers.getHours() != time.getHour()) {
      registers.setHours(time.getHour(), true);
      change = true;
    }
    if (registers.getMinutes() != time.getMinute()) {
      registers.setMinutes(time.getMinute());
      change = true;
    }
    if (registers.getSeconds() != time.getSecond()) {
      registers.setSeconds(time.getSecond());
      change = true;
    }
    if (change) {
      write();
    }
  }

  public Alarm getAlarm1() {
    return registers.getAlarm1();
  }

  public Alarm getAlarm2() {
    return registers.getAlarm2();
  }

  @Override
  public String getName() {
    return "DS3231";
  }

  @Override
  public String getDescription() {
    return "Real Time Clock";
  }
}