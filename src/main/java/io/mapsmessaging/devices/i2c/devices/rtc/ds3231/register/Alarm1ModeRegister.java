package io.mapsmessaging.devices.i2c.devices.rtc.ds3231.register;

import io.mapsmessaging.devices.deviceinterfaces.AbstractRegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.Register;
import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.data.Alarm1SettingsData;
import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.values.Alarm1Settings;

import java.io.IOException;

public class Alarm1ModeRegister extends Register {

  private final SecondsRegister secondsRegister;
  private final MinutesRegister minutesRegister;
  private final HourRegister hourRegister;
  private final AlarmDayRegister dayRegister;

  public Alarm1ModeRegister(
      I2CDevice sensor,
      SecondsRegister secondsRegister,
      MinutesRegister minutesRegister,
      HourRegister hourRegister,
      AlarmDayRegister dayRegister) {
    super(sensor, 0x80, "");
    this.secondsRegister = secondsRegister;
    this.minutesRegister = minutesRegister;
    this.hourRegister = hourRegister;
    this.dayRegister = dayRegister;
  }

  public void setAlarmSettings(Alarm1Settings settings) throws IOException {
    int mode = settings.getMask();
    boolean setDay = settings.isDay();
    secondsRegister.setTop((mode & 0b0001) != 0);
    minutesRegister.setTop((mode & 0b0010) != 0);
    hourRegister.setTop((mode & 0b0100) != 0);
    dayRegister.setTop((mode & 0b1000) != 0);
    dayRegister.setDate(!setDay);
  }

  public Alarm1Settings getAlarmSettings() throws IOException {
    int mode = 0;
    boolean setDay = !dayRegister.isDate();
    if(secondsRegister.isTopSet()){
      mode = 0b0001;
    }
    if(minutesRegister.isTopSet()){
      mode = mode | 0b0010;
    }
    if(hourRegister.isTopSet()){
      mode = mode | 0b0100;
    }
    if(dayRegister.isTopSet()){
      mode = mode | 0b1000;
    }
    return Alarm1Settings.find(mode, setDay);
  }


  @Override
  public boolean fromData(AbstractRegisterData input) throws IOException {
    if (input instanceof Alarm1SettingsData) {
      Alarm1SettingsData data = (Alarm1SettingsData) input;
      setAlarmSettings(data.getAlarmSettings());
      return true;
    }
    return false;
  }

  @Override
  public AbstractRegisterData toData() throws IOException {
    return new Alarm1SettingsData(getAlarmSettings());
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
