/*
 *      Copyright [ 2020 - 2023 ] [Matthew Buckton]
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package io.mapsmessaging.devices.i2c.devices.output.lcd.lcd1602.commands;

import static io.mapsmessaging.devices.i2c.devices.output.lcd.lcd1602.commands.Constants.CONTROL;
import static io.mapsmessaging.devices.i2c.devices.output.lcd.lcd1602.commands.Constants.ENTRY_MODE_SET;

public class EntryModeSet extends Command {
  public EntryModeSet() {
    super(CONTROL, ENTRY_MODE_SET);
  }

  public void setIncrementByOne(boolean flag) {
    if (flag) {
      buffer[1] = (byte) (buffer[1] | 0b10);
    } else {
      buffer[1] = (byte) (buffer[1] & 0b101);
    }
  }

  public void shitDisplay(boolean flag) {
    if (flag) {
      buffer[1] = (byte) (buffer[1] | 0b1);
    } else {
      buffer[1] = (byte) (buffer[1] & 0b110);
    }

  }

}
