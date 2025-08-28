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
import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.data.StatusData;

import java.io.IOException;

public class StatusRegister extends SingleByteRegister {

  private static final int OSC_STOPPED = 0b10000000;
  private static final int ENABLE_32_K = 0b00001000;

  private static final int BUSY = 0b00000100;
  private static final int ALARM2_ACTIVE = 0b00000010;
  private static final int ALARM1_ACTIVE = 0b00000001;

  public StatusRegister(I2CDevice device) throws IOException {
    super(device, 0xF, "STATUS");
  }

  public boolean isOscillatorStopped() throws IOException {
    reload();
    return (registerValue & OSC_STOPPED) != 0;
  }

  public boolean isEnabled32K() throws IOException {
    reload();
    return (registerValue & ENABLE_32_K) != 0;
  }

  public void setEnable32K(boolean flag) throws IOException {
    setControlRegister(~ENABLE_32_K, flag ? ENABLE_32_K : 0);
  }

  public boolean isBusy() throws IOException {
    reload();
    return (registerValue & BUSY) != 0;
  }

  public boolean isAlarm2FlagSet() throws IOException {
    reload();
    return (registerValue & ALARM2_ACTIVE) != 0;
  }

  public boolean isAlarm1FlagSet() throws IOException {
    reload();
    return (registerValue & ALARM1_ACTIVE) != 0;
  }

  public void clearAlarm2Flag() throws IOException {
    setControlRegister(~ALARM2_ACTIVE, 0);
  }

  public void clearAlarm1Flag() throws IOException {
    setControlRegister(~ALARM1_ACTIVE, 0);
  }

  @Override
  public boolean fromData(RegisterData input) throws IOException {
    if (input instanceof StatusData data) {
      setEnable32K(data.isEnable32K());
      if (data.isAlarm2FlagSet()) {
        clearAlarm2Flag();
      }
      if (data.isAlarm1FlagSet()) {
        clearAlarm1Flag();
      }
      return true;
    }
    return false;
  }

  @Override
  public RegisterData toData() throws IOException {
    return new StatusData(
        isOscillatorStopped(),
        isEnabled32K(),
        isBusy(),
        isAlarm2FlagSet(),
        isAlarm1FlagSet()
    );
  }
}

