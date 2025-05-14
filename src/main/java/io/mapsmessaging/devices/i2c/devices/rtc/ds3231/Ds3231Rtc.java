/*
 *
 *  Copyright [ 2020 - 2024 ] [Matthew Buckton]
 *  Copyright [ 2024 - 2025.  ] [Maps Messaging B.V.]
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package io.mapsmessaging.devices.i2c.devices.rtc.ds3231;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.deviceinterfaces.Clock;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.register.*;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.devices.logging.DeviceLogMessage;
import io.mapsmessaging.devices.sensorreadings.FloatSensorReading;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import io.mapsmessaging.logging.LoggerFactory;
import lombok.Getter;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static io.mapsmessaging.devices.logging.DeviceLogMessage.I2C_BUS_INVALID_DATE;
import static io.mapsmessaging.devices.logging.DeviceLogMessage.I2C_BUS_INVALID_TIME;

@Getter
public class Ds3231Rtc extends I2CDevice implements Clock, Sensor {

  private final SecondsRegister secondsRegister;
  @Getter
  private final MinutesRegister minutesRegister;
  @Getter
  private final HourRegister hourRegister;
  @Getter
  private final WeekDayRegister weekDayRegister;
  @Getter
  private final MonthDayRegister monthDayRegister;
  @Getter
  private final MonthRegister monthRegister;
  @Getter
  private final YearRegister yearRegister;
  @Getter
  private final SecondsRegister alarm1Seconds;
  @Getter
  private final MinutesRegister alarm1Minutes;
  @Getter
  private final HourRegister alarm1Hours;
  @Getter
  private final AlarmDayRegister alarm1DayRegister;
  @Getter
  private final MinutesRegister alarm2Minutes;
  @Getter
  private final HourRegister alarm2Hours;
  @Getter
  private final AlarmDayRegister alarm2DayRegister;
  @Getter
  private final ControlRegister controlRegister;
  @Getter
  private final StatusRegister statusRegister;
  @Getter
  private final AgingRegister agingRegister;
  @Getter
  private final TemperatureRegister temperatureRegister;
  @Getter
  private final Alarm1ModeRegister alarm1ModeRegister;
  @Getter
  private final Alarm2ModeRegister alarm2ModeRegister;
  @Getter
  private final List<SensorReading<?>> readings;
  public Ds3231Rtc(AddressableDevice device) throws IOException {
    super(device, LoggerFactory.getLogger(Ds3231Rtc.class));
    secondsRegister = new SecondsRegister(this, 0x0, "SECONDS");
    minutesRegister = new MinutesRegister(this, 0x1, "MINUTES");
    hourRegister = new HourRegister(this, 0x2, "HOURS");
    weekDayRegister = new WeekDayRegister(this); //3
    monthDayRegister = new MonthDayRegister(this); //4
    monthRegister = new MonthRegister(this); //5
    yearRegister = new YearRegister(this); //6

    alarm1Seconds = new SecondsRegister(this, 0x7, "ALARM1_SECONDS");
    alarm1Minutes = new MinutesRegister(this, 0x8, "ALARM1_MINUTES");
    alarm1Hours = new HourRegister(this, 0x9, "ALARM1_HOURS");
    alarm1DayRegister = new AlarmDayRegister(this, 0xA, "ALARM1_DAY");

    alarm2Minutes = new MinutesRegister(this, 0xB, "ALARM2_MINUTES");
    alarm2Hours = new HourRegister(this, 0xC, "ALARM2_HOURS");
    alarm2DayRegister = new AlarmDayRegister(this, 0xD, "ALARM2_DAY");

    controlRegister = new ControlRegister(this);
    statusRegister = new StatusRegister(this);
    agingRegister = new AgingRegister(this);
    temperatureRegister = new TemperatureRegister(this);

    alarm1ModeRegister = new Alarm1ModeRegister(this, alarm1Seconds, alarm1Minutes, alarm1Hours, alarm1DayRegister);
    alarm2ModeRegister = new Alarm2ModeRegister(this, alarm2Minutes, alarm2Hours, alarm2DayRegister);

    SensorReading<Float> temperature = new FloatSensorReading(
        "temperature",
        "°C",
        "Temperature reading from DS3231 RTC",
        25.0f,
        true,
        -10f,
        60f,
        1,
        this::getTemperature
    );

    SensorReading<LocalDateTime> dateTime = new LocalDateTimeSensorReading(
        "date",
        "UTC",
        "Timestamp from DS3231 RTC",
        LocalDateTime.of(2024, 1, 1, 0, 0),
        true,
        this::getDateTime
    );


    readings = List.of(temperature, dateTime);
  }

  public static boolean detect(AddressableDevice i2cDevice) throws IOException {
    try (Ds3231Rtc t = new Ds3231Rtc(i2cDevice)) {
      return t.isConnected();
    }
  }

  @Override
  public boolean isConnected() {

    try {
      int t = (secondsRegister.getSeconds() + 1) % 60;
      long end = System.currentTimeMillis() + 1000;
      while (end > System.currentTimeMillis()) {
        int next = secondsRegister.getSeconds();
        if (t == next) {
          return true;
        }
      }
    } catch (IOException e) {
      // ignore
    }
    return false;
  }

  protected float getTemperature() throws IOException {
    float val = temperatureRegister.getTemperature();
    if (logger.isDebugEnabled()) {
      logger.log(DeviceLogMessage.I2C_BUS_DEVICE_WRITE_REQUEST, getName(), val + " = getTemperature()");
    }
    return val;
  }

  public LocalDateTime getDateTime() throws IOException {
    LocalDateTime dateTime = LocalDateTime.of(getDate(), getTime());
    if (logger.isDebugEnabled()) {
      logger.log(DeviceLogMessage.I2C_BUS_DEVICE_WRITE_REQUEST, getName(), dateTime + " = getDateTime()");
    }
    return dateTime;
  }

  @Override
  public void setDateTime(LocalDateTime dateTime) throws IOException {
    if (logger.isDebugEnabled()) {
      logger.log(DeviceLogMessage.I2C_BUS_DEVICE_WRITE_REQUEST, getName(), "setDateTime(" + dateTime + ")");
    }
    secondsRegister.setSeconds(dateTime.getSecond());
    minutesRegister.setMinutes(dateTime.getMinute());
    hourRegister.setClock24Mode(true);
    hourRegister.setHours(dateTime.getHour());
    monthDayRegister.setDate(dateTime.getDayOfMonth());
    monthRegister.setMonth(dateTime.getMonth().getValue());
    yearRegister.setYear(dateTime.getYear());
  }

  @Override
  public void setAlarm(int alarmNumber, LocalDateTime dateTime) throws IOException {
    if (alarmNumber == 1) {
      alarm1Seconds.setSeconds(dateTime.getSecond());
      alarm1Minutes.setMinutes(dateTime.getMinute());
      alarm1Hours.setClock24Mode(true);
      alarm1Hours.setHours(dateTime.getHour());
      alarm1DayRegister.setDate(true);
      alarm1DayRegister.setDay(dateTime.getDayOfMonth());
    }
    if (alarmNumber == 2) {
      alarm2Minutes.setMinutes(dateTime.getMinute());
      alarm2Hours.setClock24Mode(true);
      alarm2Hours.setHours(dateTime.getHour());
      alarm2DayRegister.setDate(true);
      alarm2DayRegister.setDay(dateTime.getDayOfMonth());
    }
  }

  @Override
  public void setAlarm(int alarmNumber, LocalTime time) throws IOException {
    if (alarmNumber == 1) {
      alarm1Seconds.setSeconds(time.getSecond());
      alarm1Minutes.setMinutes(time.getMinute());
      alarm1Hours.setClock24Mode(true);
      alarm1Hours.setHours(time.getHour());
      alarm1DayRegister.setDate(false);
      alarm1DayRegister.setDay(0);
    }
    if (alarmNumber == 2) {
      alarm2Minutes.setMinutes(time.getMinute());
      alarm2Hours.setClock24Mode(true);
      alarm2Hours.setHours(time.getHour());
      alarm2DayRegister.setDate(false);
      alarm2DayRegister.setDay(0);
    }
  }

  public LocalDate getDate() throws IOException {
    LocalDate localDate;
    int year = yearRegister.getYear();
    int month = monthRegister.getMonth();
    int date = monthDayRegister.getDate();
    try {
      localDate = LocalDate.of(year, month, date);
    } catch (DateTimeException invalid) {
      logger.log(I2C_BUS_INVALID_DATE, year, month, date);
      localDate = LocalDate.now();
    }
    if (logger.isDebugEnabled()) {
      logger.log(DeviceLogMessage.I2C_BUS_DEVICE_WRITE_REQUEST, getName(), localDate + " = getDate()");
    }
    return localDate;
  }

  @Override
  public void setDate(LocalDate date) throws IOException {
    if (logger.isDebugEnabled()) {
      logger.log(DeviceLogMessage.I2C_BUS_DEVICE_WRITE_REQUEST, getName(), "setDate(" + date + ")");
    }
    monthDayRegister.setDate(date.getDayOfMonth());
    monthRegister.setMonth(date.getMonth().getValue());
    yearRegister.setYear(date.getYear());

  }

  public LocalTime getTime() throws IOException {
    LocalTime localTime;
    int hour = hourRegister.getHours();
    int minute = minutesRegister.getMinutes();
    int second = secondsRegister.getSeconds();
    try {
      localTime = LocalTime.of(hour, minute, second);
    } catch (DateTimeException e) {
      logger.log(I2C_BUS_INVALID_TIME, hour, minute, second);
      localTime = LocalTime.now();
    }
    if (logger.isDebugEnabled()) {
      logger.log(DeviceLogMessage.I2C_BUS_DEVICE_WRITE_REQUEST, getName(), localTime + " = getTime()");
    }
    return localTime;
  }

  @Override
  public void setTime(LocalTime time) throws IOException {
    if (logger.isDebugEnabled()) {
      logger.log(DeviceLogMessage.I2C_BUS_DEVICE_WRITE_REQUEST, getName(), "setTime(" + time + ")");
    }
    secondsRegister.setSeconds(time.getSecond());
    minutesRegister.setMinutes(time.getMinute());
    hourRegister.setClock24Mode(true);
    hourRegister.setHours(time.getHour());
  }

  @Override
  public String getName() {
    return "DS3231";
  }

  @Override
  public String getDescription() {
    return "Real Time Clock";
  }


  @Override
  public DeviceType getType() {
    return DeviceType.CLOCK;
  }
}