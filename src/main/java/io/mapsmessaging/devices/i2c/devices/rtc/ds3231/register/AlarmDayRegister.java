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
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.data.AlarmDaySettingsData;

import java.io.IOException;

public class AlarmDayRegister extends SingleByteRegister {
  private static final int DAY = 0b00000111;
  private static final int DATE = 0b00001111;
  private static final int TEN_DATE = 0b00110000;
  private static final int DATE_MASK = 0b00111111;
  private static final int DAY_DATE = 0b01000000;
  private static final int TOP = 0b10000000;

  public AlarmDayRegister(I2CDevice sensor, int address, String name) throws IOException {
    super(sensor, address, name);
    reload();
  }

  public boolean isTopSet() {
    return (registerValue & TOP) != 0;
  }

  public void setTop(boolean flag) throws IOException {
    setControlRegister(~TOP, flag ? TOP : 0);
  }

  public boolean isDate() {
    return (registerValue & DAY_DATE) != 0;
  }

  public void setDate(boolean flag) throws IOException {
    setControlRegister(~DAY_DATE, flag ? DAY_DATE : 0);
  }

  public int getDay() throws IOException {
    reload();
    if ((registerValue & DAY_DATE) != 0) {
      return ((registerValue & TEN_DATE) >> 4) * 10 + registerValue & DATE;
    }
    return registerValue & DAY;
  }

  public void setDay(int day) throws IOException {
    int value = day & DAY;
    if ((registerValue & DAY_DATE) != 0) {
      value = (day / 10) << 4 | day % 10;
    }
    super.setControlRegister(~DATE_MASK, value);
  }

  @Override
  public boolean fromData(RegisterData input) throws IOException {
    if (input instanceof AlarmDaySettingsData) {
      AlarmDaySettingsData data = (AlarmDaySettingsData) input;
      setTop(data.isTop());
      setDate(data.isDate());
      setDay(data.getDay());
      return true;
    }
    return false;
  }

  @Override
  public RegisterData toData() throws IOException {
    return new AlarmDaySettingsData(isTopSet(), isDate(), getDay());
  }
}
