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

package io.mapsmessaging.devices.i2c.devices.output.lcd.lcd1602.commands;

import static io.mapsmessaging.devices.i2c.devices.output.lcd.lcd1602.commands.Constants.CONTROL;

public class EntryModeSet extends Command {

  private static final byte ENTRY_MODE_SET = 0x04;
  private static final byte ENTRY_LEFT = 0x02;
  private static final byte ENTRY_SHIFT_INCREMENT = 0x01;

  public EntryModeSet() {
    super(CONTROL, ENTRY_MODE_SET);
  }

  public void leftToRight() {
    buffer[1] = (byte) (buffer[1] | ENTRY_LEFT);
  }

  public void rightToLeft() {
    buffer[1] = (byte) (buffer[1] & ~ENTRY_LEFT);
  }

  public void noAutoScroll() {
    buffer[1] = (byte) (buffer[1] & ~ENTRY_SHIFT_INCREMENT);
  }

  public void autoScroll() {
    buffer[1] = (byte) (buffer[1] | ENTRY_SHIFT_INCREMENT);
  }
}
