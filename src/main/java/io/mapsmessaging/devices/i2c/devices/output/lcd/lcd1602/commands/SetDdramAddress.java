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

package io.mapsmessaging.devices.i2c.devices.output.lcd.lcd1602.commands;

import static io.mapsmessaging.devices.i2c.devices.output.lcd.lcd1602.commands.Constants.CONTROL;

public class SetDdramAddress extends Command {

  private static final byte SET_DDRAM_ADDR = (byte) 0x80;

  public SetDdramAddress() {
    super(CONTROL, SET_DDRAM_ADDR);
  }

  public void setCursor(byte row, byte col) {
    buffer[1] = (byte) (SET_DDRAM_ADDR | (row == 0 ? col : col | 0x40));
  }
}
