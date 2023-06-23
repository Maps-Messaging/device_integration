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
import io.mapsmessaging.devices.i2c.devices.sensors.ds3231.register.AlarmRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.ds3231.register.ControlRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.ds3231.register.Registers;
import io.mapsmessaging.devices.i2c.devices.sensors.ds3231.register.StatusRegister;
import io.mapsmessaging.logging.Logger;
import io.mapsmessaging.logging.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Ds3231Rtc extends I2CDevice {

  private final Logger logger = LoggerFactory.getLogger(Ds3231Rtc.class);

  private final Registers registers;

  public Ds3231Rtc(I2C device) {
    super(device);
    registers = new Registers(device);
    read();
  }

  @Override
  public boolean isConnected() {
    return true;
  }

  protected void read() {
    byte[] registerRead = new byte[19];
    for (int x = 0; x < registerRead.length; x++) {
      registerRead[x] = (byte) (readRegister(x) & 0xff);
    }
    registers.setRegisterValues(registerRead);
  }

  public float getTemperature(){
    return registers.getTemperature();
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
    if (registers.getMonth() != date.getMonthValue()) {
      registers.setMonth(date.getMonthValue());
    }
    if (registers.getDate() != date.getDayOfMonth()) {
      registers.setDate(date.getDayOfMonth());
    }
    if (registers.getYear() != date.getYear()) {
      registers.setYear(date.getYear());
    }
    if (registers.getDayOfWeek() != date.getDayOfWeek().getValue()) {
      registers.setDayOfWeek(date.getDayOfWeek().getValue());
    }
  }

  public LocalTime getTime() {
    return LocalTime.of(registers.getHours(), registers.getMinutes(), registers.getSeconds());
  }

  public void setTime(LocalTime time) {
    if (registers.getHours() != time.getHour()) {
      registers.setHours(time.getHour(), false);
    }
    if (registers.getMinutes() != time.getMinute()) {
      registers.setMinutes(time.getMinute());
    }
    if (registers.getSeconds() != time.getSecond()) {
      registers.setSeconds(time.getSecond());
    }
  }

  public AlarmRegister getAlarm1() {
    return registers.getAlarm1();
  }

  public AlarmRegister getAlarm2() {
    return registers.getAlarm2();
  }

  public ControlRegister getControlRegister(){
    return registers.getControlRegister();
  }

  public StatusRegister getStatusRegiser(){
    return registers.getStatusRegister();
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