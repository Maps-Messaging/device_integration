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

public class DisplayControl extends Command {
  private static final byte DISPLAY_CONTROL = 0x08;

  private static final byte DISPLAY_ON = 0x04;
  private static final byte CURSOR_ON = 0x02;
  private static final byte BLINK_ON = 0x01;

  public DisplayControl() {
    super(CONTROL, DISPLAY_CONTROL);
  }

  public void setDisplayOn(boolean flag) {
    if (flag) {
      buffer[1] = (byte) (buffer[1] | DISPLAY_ON);
    } else {
      buffer[1] = (byte) (buffer[1] & ~DISPLAY_ON);
    }
  }

  public void setCursorOn(boolean flag) {
    if (flag) {
      buffer[1] = (byte) (buffer[1] | CURSOR_ON);
    } else {
      buffer[1] = (byte) (buffer[1] & ~CURSOR_ON);
    }
  }

  public void setBlinkingOn(boolean flag) {
    if (flag) {
      buffer[1] = (byte) (buffer[1] | BLINK_ON);
    } else {
      buffer[1] = (byte) (buffer[1] & ~BLINK_ON);
    }
  }
}
