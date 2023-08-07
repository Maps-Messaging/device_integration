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

public class CursorControl extends Command {

  private static final byte CURSOR_SHIFT = 0x10;

  private static final byte DISPLAY_MOVE = 0x08;
  private static final byte CURSOR_MOVE = 0x00;
  private static final byte MOVE_RIGHT = 0x04;
  private static final byte MOVE_LEFT = 0x00;

  public CursorControl() {
    super(CONTROL, CURSOR_SHIFT);
  }

  public void moveCursor(boolean flag) {
    if (flag) {
      buffer[1] = (byte) (buffer[1] | 0b01000);
    } else {
      buffer[1] = (byte) (buffer[1] & 0b10111);
    }
  }

  public void moveCursorLeft() {
    buffer[1] = CURSOR_SHIFT | CURSOR_MOVE | MOVE_LEFT;
  }

  public void moveCursorRight() {
    buffer[1] = CURSOR_SHIFT | CURSOR_MOVE | MOVE_RIGHT;
  }

  public void scrollDisplayLeft() {
    buffer[1] = CURSOR_SHIFT | DISPLAY_MOVE | MOVE_LEFT;
  }

  public void scrollDisplayRight() {
    buffer[1] = CURSOR_SHIFT | DISPLAY_MOVE | MOVE_RIGHT;
  }

}
