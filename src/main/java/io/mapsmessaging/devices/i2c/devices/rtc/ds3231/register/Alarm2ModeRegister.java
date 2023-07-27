package io.mapsmessaging.devices.i2c.devices.rtc.ds3231.register;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.Register;
import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.values.Alarm2Settings;

import java.io.IOException;

public class Alarm2ModeRegister extends Register {

  private final MinutesRegister minutesRegister;
  private final HourRegister hourRegister;
  private final AlarmDayRegister dayRegister;

  public Alarm2ModeRegister(
      I2CDevice sensor,
      MinutesRegister minutesRegister,
      HourRegister hourRegister,
      AlarmDayRegister dayRegister) {
    super(sensor, 0, "");
    this.minutesRegister = minutesRegister;
    this.hourRegister = hourRegister;
    this.dayRegister = dayRegister;
  }

  public void setAlarmSettings(Alarm2Settings settings) throws IOException {
    int mode = settings.getMask();
    boolean setDay = settings.isDay();
    minutesRegister.setTop((mode & 0b001) != 0);
    hourRegister.setTop((mode & 0b010) != 0);
    dayRegister.setTop((mode & 0b100) != 0);
    dayRegister.setDate(!setDay);
  }

  public Alarm2Settings getAlarmSettings() throws IOException {
    int mode = 0;
    boolean setDay = !dayRegister.isDate();
    if(minutesRegister.isTopSet()){
      mode = mode | 0b001;
    }
    if(hourRegister.isTopSet()){
      mode = mode | 0b010;
    }
    if(dayRegister.isTopSet()){
      mode = mode | 0b100;
    }
    return Alarm2Settings.find(mode, setDay);
  }


  @Override
  protected void reload() throws IOException {
  }

  @Override
  protected void setControlRegister(int mask, int value) throws IOException {

  }

  @Override
  public String toString(int maxLength) {
    return "";
  }
}