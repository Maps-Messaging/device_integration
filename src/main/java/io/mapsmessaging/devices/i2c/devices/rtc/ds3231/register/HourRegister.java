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

package io.mapsmessaging.devices.i2c.devices.rtc.ds3231.register;

import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;
import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.data.HourData;

import java.io.IOException;

public class HourRegister extends SingleByteRegister {

  private static final int TOP = 0b10000000;
  private static final int CLOCK_24_MODE = 0b01000000;
  private static final int AM_PM = 0b00100000;
  private static final int TEN_HOURS = 0b00010000;
  private static final int HOURS = 0b00001111;

  public HourRegister(I2CDevice sensor, int address, String name) throws IOException {
    super(sensor, address, name);
    reload();
  }

  public boolean isTopSet() {
    return (registerValue & TOP) != 0;
  }

  public void setTop(boolean flag) throws IOException {
    setControlRegister(~TOP, flag ? TOP : 0);
  }

  public boolean getClock24Mode() {
    return (registerValue & CLOCK_24_MODE) != 0;
  }

  public void setClock24Mode(boolean flag) throws IOException {
    setControlRegister(~CLOCK_24_MODE, flag ? CLOCK_24_MODE : 0);
  }

  public boolean isPM() throws IOException {
    reload();
    return (registerValue & AM_PM) != 0;
  }

  public void setPM(boolean flag) throws IOException {
    setControlRegister(~AM_PM, flag ? AM_PM : 0);
  }

  public int getHours() throws IOException {
    reload();
    int hours = registerValue & HOURS;
    hours = BcdRegister.bcdToDecimal(hours);
    if ((registerValue & TEN_HOURS) != 0) {
      hours += 10;
    }
    if (isPM() && getClock24Mode()) {
      hours += 12;
    }
    return hours;
  }

  public void setHours(int val) throws IOException {
    if (val > 12) {
      setPM(true);
      val -= 12;
    }
    if (val > 9) {
      setControlRegister(~TEN_HOURS, TEN_HOURS);
      val -= 10;
    } else {
      setControlRegister(~TEN_HOURS, 0);
    }
    setControlRegister(~HOURS, BcdRegister.decimalToBcd(val));
  }

  @Override
  public boolean fromData(RegisterData input) throws IOException {
    if (input instanceof HourData) {
      HourData data = (HourData) input;
      setTop(data.isTopSet());
      setClock24Mode(data.isClock24Mode());
      setPM(data.isPm());
      setHours(data.getHours());
      return true;
    }
    return false;
  }

  @Override
  public RegisterData toData() throws IOException {
    return new HourData(
        isTopSet(),
        getClock24Mode(),
        isPM(),
        getHours());
  }
}
