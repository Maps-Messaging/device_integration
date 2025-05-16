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
import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.data.MonthData;

import java.io.IOException;

public class MonthRegister extends BcdRegister {

  private static final int CENTURY = 0b10000000;

  public MonthRegister(I2CDevice sensor) throws IOException {
    super(sensor, 0x5, "MONTH", false);
    reload();
  }

  public boolean isCentury() {
    return (registerValue & CENTURY) != 0;
  }

  public void setCentury(boolean flag) throws IOException {
    setControlRegister(~CENTURY, flag ? CENTURY : 0);
  }

  public int getMonth() throws IOException {
    reload();
    return super.getValue();
  }

  public void setMonth(int val) throws IOException {
    super.setValue(val);
  }

  @Override
  public boolean fromData(RegisterData input) throws IOException {
    if (input instanceof MonthData) {
      MonthData data = (MonthData) input;
      setMonth(data.getMonth());
      setCentury(data.isCentury());
      return true;
    }
    return false;
  }

  @Override
  public RegisterData toData() throws IOException {
    return new MonthData(getMonth(), isCentury());
  }
}

