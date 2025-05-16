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

package io.mapsmessaging.devices.i2c.devices.output.led.ht16k33;

import java.util.Arrays;
import java.util.Base64;

public class Panel {

  private final byte[] display;

  private final boolean hasColon;

  public Panel(int size, boolean hasColon) {
    this.hasColon = hasColon;
    display = new byte[hasColon ? (size + 1) * 2 + 1 : size * 2];
  }

  public String pack() {
    return Base64.getEncoder().encodeToString(display);
  }

  public void setAllDisplay(int mask) {
    int end = hasColon ? display.length - 1 : display.length;
    for (int x = 0; x < end; x++) {
      setDisplay(x, mask);
    }
  }

  public void setDisplay(int position, int mask) {
    int actual = position;
    if (position > 1) {
      actual = hasColon ? position + 1 : position;
    }
    actual = actual * 2;
    if (actual > display.length) {
      return;
    }
    display[actual] = (byte) (mask & 0xff);
  }

  public void enableColon(boolean flag) {
    if (!hasColon) {
      return;
    }
    if (flag) {
      display[5] = (byte) (0xff);
    }
    display[5] = (byte) (0x0);
  }

  public void clear() {
    Arrays.fill(display, (byte) (0x0));
  }

}
