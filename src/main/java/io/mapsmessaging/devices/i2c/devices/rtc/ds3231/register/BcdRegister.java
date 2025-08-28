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

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.SingleByteRegister;

import java.io.IOException;

public class BcdRegister extends SingleByteRegister {

  private static final int TOP = 0b10000000;
  private static final int TENS = 0b01110000;
  private static final int UNITSS = 0b00001111;

  private static final int BCD_FLAG = 0b01111111;
  private static final int BCD_TOP_FLAG = 0b11111111;

  private final int bcdMask;

  public BcdRegister(I2CDevice sensor, int address, String name, boolean includeTop) throws IOException {
    super(sensor, address, name);
    if (includeTop) {
      bcdMask = BCD_TOP_FLAG;
    } else {
      bcdMask = BCD_FLAG;
    }
  }

  public static int bcdToDecimal(int bcdValue) {
    return ((bcdValue & TENS) >> 4) * 10 + (bcdValue & UNITSS);
  }

  protected static byte decimalToBcd(int decimalValue) {
    return (byte) (((decimalValue / 10) << 4) | (decimalValue % 10));
  }

  public boolean isTopSet() {
    return (registerValue & TOP) != 0;
  }

  public void setTop(boolean flag) throws IOException {
    setControlRegister(~TOP, flag ? TOP : 0);
  }

  protected int getValue() throws IOException {
    reload();
    return bcdToDecimal(registerValue & bcdMask);
  }

  protected void setValue(int value) throws IOException {
    setControlRegister(~bcdMask, decimalToBcd(value));
  }
}