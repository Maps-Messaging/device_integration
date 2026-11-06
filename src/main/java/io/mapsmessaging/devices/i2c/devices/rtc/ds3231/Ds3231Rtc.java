/*
 *
 *  Copyright [ 2020 - 2024 ] Matthew Buckton
 *  Copyright [ 2024 - 2026 ] MapsMessaging B.V.
 *
 *  Licensed under the Apache License, Version 2.0 with the Commons Clause
 *  (the "License"); you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      https://commonsclause.com/
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */

package io.mapsmessaging.devices.i2c.devices.rtc.ds3231;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.deviceinterfaces.Clock;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.register.AgingRegister;
import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.register.Alarm1ModeRegister;
import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.register.Alarm2ModeRegister;
import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.register.AlarmDayRegister;
import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.register.ControlRegister;
import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.register.HourRegister;
import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.register.MinutesRegister;
import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.register.MonthDayRegister;
import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.register.MonthRegister;
import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.register.SecondsRegister;
import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.register.StatusRegister;
import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.register.TemperatureRegister;
import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.register.WeekDayRegister;
import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.register.YearRegister;
import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.values.Alarm1Settings;
import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.values.Alarm2Settings;
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
  private boolean temperatureInitialized;
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
        LocalDateTime.of(2024, java.time.Month.JANUARY, 1, 0, 0),
        true,
        this::getDateTime
    );


    readings = List.of(temperature, dateTime);
  }

  public static boolean detect(AddressableDevice i2cDevice) throws IOException {
    if (i2cDevice.getDevice() != 0x68) return false;
    try {
      byte[] data = new byte[19];
      if (i2cDevice.readRegister(0, data, 0, data.length) != data.length) {
        throw new IOException("Incomplete DS3231 detection response");
      }
      if (!validBcd(data[0] & 0xff, 0, 59) || !validBcd(data[1] & 0xff, 0, 59)) return false;
      int hour = data[2] & 0xff;
      if ((hour & 0x80) != 0) return false;
      if ((hour & 0x40) == 0) {
        if (!validBcd(hour, 0, 23)) return false;
      } else if (!validBcd(hour & 0x1f, 1, 12)) return false;
      int weekday = data[3] & 0xff;
      int date = data[4] & 0xff;
      int month = data[5] & 0xff;
      int year = data[6] & 0xff;
      if (weekday < 1 || weekday > 7 || !validBcd(date, 1, 31)
          || (month & 0x60) != 0 || !validBcd(month & 0x1f, 1, 12) || !validBcd(year, 0, 99)
          || (data[15] & 0x70) != 0 || (data[18] & 0x3f) != 0) return false;
      try {
        LocalDate.of(2000 + ((month & 0x80) != 0 ? 100 : 0) + fromBcd(year), fromBcd(month & 0x1f), fromBcd(date));
      } catch (DateTimeException invalid) {
        return false;
      }
      // Exclude a successful MPU-60x0 identity read. DS3231 itself has no chip ID,
      // and its backend may reject registers outside the documented 0x00..0x12 map.
      try {
        return i2cDevice.readRegister(0x75) != 0x68;
      } catch (com.pi4j.exception.Pi4JException unsupportedRegister) {
        return true;
      }
    } catch (com.pi4j.exception.Pi4JException exception) {
      throw new IOException("Unable to probe DS3231 registers", exception);
    }
  }

  private static boolean validBcd(int value, int minimum, int maximum) {
    return (value & 0x0f) <= 9 && ((value >> 4) & 0x0f) <= 9
        && fromBcd(value) >= minimum && fromBcd(value) <= maximum;
  }

  private static int fromBcd(int value) {
    return ((value >> 4) & 0x0f) * 10 + (value & 0x0f);
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
    ensureFirstTemperatureConversion();
    float val = temperatureRegister.getTemperature();
    if (logger.isDebugEnabled()) {
      logger.log(DeviceLogMessage.I2C_BUS_DEVICE_WRITE_REQUEST, getName(), val + " = getTemperature()");
    }
    return val;
  }

  private synchronized void ensureFirstTemperatureConversion() throws IOException {
    if (temperatureInitialized) {
      return;
    }
    long deadline = System.nanoTime() + 500_000_000L;
    while (statusRegister.isBusy()) {
      waitForTemperatureConversion(deadline);
    }
    int control = readRegister(0x0e);
    write(0x0e, (byte) (control | 0x20));
    while ((readRegister(0x0e) & 0x20) != 0 || statusRegister.isBusy()) {
      waitForTemperatureConversion(deadline);
    }
    temperatureInitialized = true;
  }

  private void waitForTemperatureConversion(long deadline) throws IOException {
    if (System.nanoTime() >= deadline) {
      throw new IOException("Timeout waiting for DS3231 initial temperature conversion");
    }
    try {
      Thread.sleep(2);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new IOException("Interrupted waiting for DS3231 initial temperature conversion", exception);
    }
  }

  private void requireValidClock() throws IOException {
    if (statusRegister.isOscillatorStopped()) {
      throw new IOException("DS3231 clock requires synchronization after oscillator stop");
    }
  }

  public LocalDateTime getDateTime() throws IOException {
    requireValidClock();
    byte[] snapshot = new byte[7];
    if (readRegister(0, snapshot, 0, snapshot.length) != snapshot.length) {
      throw new IOException("Incomplete DS3231 timestamp snapshot");
    }
    int seconds = fromBcd(snapshot[0] & 0x7f);
    int minutes = fromBcd(snapshot[1] & 0x7f);
    int hourRegisterValue = snapshot[2] & 0xff;
    int hours;
    if ((hourRegisterValue & 0x40) == 0) {
      hours = fromBcd(hourRegisterValue & 0x3f);
    } else {
      int hour12 = fromBcd(hourRegisterValue & 0x1f);
      hours = (hour12 % 12) + ((hourRegisterValue & 0x20) != 0 ? 12 : 0);
    }
    int day = fromBcd(snapshot[4] & 0x3f);
    int month = fromBcd(snapshot[5] & 0x1f);
    int year = 2000 + ((snapshot[5] & 0x80) != 0 ? 100 : 0) + fromBcd(snapshot[6] & 0xff);
    LocalDateTime dateTime;
    try {
      dateTime = LocalDateTime.of(year, month, day, hours, minutes, seconds);
    } catch (DateTimeException exception) {
      throw new IOException("Invalid DS3231 timestamp snapshot", exception);
    }
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
    setYearAndCentury(dateTime.getYear(), dateTime.getMonthValue());
    statusRegister.clearOscillatorStopped();
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
      alarm1ModeRegister.setAlarmSettings(Alarm1Settings.DATE_HOUR_MINUTE_SECOND);
    }
    if (alarmNumber == 2) {
      alarm2Minutes.setMinutes(dateTime.getMinute());
      alarm2Hours.setClock24Mode(true);
      alarm2Hours.setHours(dateTime.getHour());
      alarm2DayRegister.setDate(true);
      alarm2DayRegister.setDay(dateTime.getDayOfMonth());
      alarm2ModeRegister.setAlarmSettings(Alarm2Settings.DATE_HOUR_MINUTE);
    }
  }

  @Override
  public void setAlarm(int alarmNumber, LocalTime time) throws IOException {
    if (alarmNumber == 1) {
      alarm1Seconds.setSeconds(time.getSecond());
      alarm1Minutes.setMinutes(time.getMinute());
      alarm1Hours.setClock24Mode(true);
      alarm1Hours.setHours(time.getHour());
      alarm1DayRegister.setDate(true);
      alarm1DayRegister.setDay(1);
      alarm1ModeRegister.setAlarmSettings(Alarm1Settings.HOURS_MINUTES_SECONDS);
    }
    if (alarmNumber == 2) {
      alarm2Minutes.setMinutes(time.getMinute());
      alarm2Hours.setClock24Mode(true);
      alarm2Hours.setHours(time.getHour());
      alarm2DayRegister.setDate(true);
      alarm2DayRegister.setDay(1);
      alarm2ModeRegister.setAlarmSettings(Alarm2Settings.HOURS_MINUTES);
    }
  }

  public LocalDate getDate() throws IOException {
    requireValidClock();
    LocalDate localDate;
    int month = monthRegister.getMonth();
    int year = yearRegister.getYear() + (monthRegister.isCentury() ? 100 : 0);
    int date = monthDayRegister.getDate();
    try {
      localDate = LocalDate.of(year, month, date);
    } catch (DateTimeException invalid) {
      logger.log(I2C_BUS_INVALID_DATE, year, month, date);
      throw new IOException("Invalid DS3231 calendar date", invalid);
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
    setYearAndCentury(date.getYear(), date.getMonthValue());

  }

  private void setYearAndCentury(int year, int month) throws IOException {
    if (year < 2000 || year > 2199) {
      throw new IOException("DS3231 supported year range is 2000 through 2199");
    }
    monthRegister.setMonth(month);
    monthRegister.setCentury(year >= 2100);
    yearRegister.setYear(2000 + (year % 100));
  }

  public LocalTime getTime() throws IOException {
    requireValidClock();
    LocalTime localTime;
    int hour = hourRegister.getHours();
    int minute = minutesRegister.getMinutes();
    int second = secondsRegister.getSeconds();
    try {
      localTime = LocalTime.of(hour, minute, second);
    } catch (DateTimeException e) {
      logger.log(I2C_BUS_INVALID_TIME, hour, minute, second);
      throw new IOException("Invalid DS3231 clock time", e);
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
