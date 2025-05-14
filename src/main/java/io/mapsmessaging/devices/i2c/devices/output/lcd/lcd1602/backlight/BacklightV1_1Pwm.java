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

package io.mapsmessaging.devices.i2c.devices.output.lcd.lcd1602.backlight;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.impl.AddressableDevice;

public class BacklightV1_1Pwm extends BacklightPwm {

  private static final byte REG_RED = 0x06;
  private static final byte REG_GREEN = 0x05;
  private static final byte REG_BLUE = 0x04;
  private static final byte REG_ONLY = 0x04;

  protected BacklightV1_1Pwm(AddressableDevice device) {
    super(device, REG_RED, REG_GREEN, REG_BLUE, REG_ONLY);
  }


  @Override
  public DeviceType getType() {
    return DeviceType.PWM;
  }
}
