/*
 *
 *  Copyright [ 2020 - 2024 ] Matthew Buckton
 *  Copyright [ 2024 - 2025 ] MapsMessaging B.V.
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

package io.mapsmessaging.devices.i2c.devices.rtc.ds3231.register;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.Register;
import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.data.Alarm2SettingsData;
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
    super(sensor, 0x81, "");
    this.minutesRegister = minutesRegister;
    this.hourRegister = hourRegister;
    this.dayRegister = dayRegister;
  }

  public Alarm2Settings getAlarmSettings() {
    int mode = 0;
    boolean setDay = !dayRegister.isDate();
    if (minutesRegister.isTopSet()) {
      mode = mode | 0b001;
    }
    if (hourRegister.isTopSet()) {
      mode = mode | 0b010;
    }
    if (dayRegister.isTopSet()) {
      mode = mode | 0b100;
    }
    return Alarm2Settings.find(mode, setDay);
  }

  public void setAlarmSettings(Alarm2Settings settings) throws IOException {
    int mode = settings.getMask();
    boolean setDay = settings.isDay();
    minutesRegister.setTop((mode & 0b001) != 0);
    hourRegister.setTop((mode & 0b010) != 0);
    dayRegister.setTop((mode & 0b100) != 0);
    dayRegister.setDate(!setDay);
  }

  @Override
  public boolean fromData(RegisterData input) throws IOException {
    if (input instanceof Alarm2SettingsData) {
      Alarm2SettingsData data = (Alarm2SettingsData) input;
      setAlarmSettings(data.getAlarmSettings());
      return true;
    }
    return false;
  }

  @Override
  public RegisterData toData() throws IOException {
    return new Alarm2SettingsData(getAlarmSettings());
  }


  @Override
  protected void reload() throws IOException {
    // No Op
  }

  @Override
  protected void setControlRegister(int mask, int value) throws IOException {
    // No Op
  }

  @Override
  public String toString(int maxLength) {
    return "";
  }
}