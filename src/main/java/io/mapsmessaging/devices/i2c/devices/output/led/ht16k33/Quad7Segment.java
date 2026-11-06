/*
 *
 *  Copyright [ 2020 - 2024 ] Matthew Buckton
 *  Copyright [ 2024 - 2026 ] MapsMessaging B.V.
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

import io.mapsmessaging.devices.impl.AddressableDevice;

import java.io.IOException;
import java.util.Arrays;

public class Quad7Segment extends HT16K33Driver {

  private final byte[] buf = new byte[10];

  public Quad7Segment(AddressableDevice device) throws IOException {
    super(device, Constants.NUMERIC_MAPPING);
    write("     ");
  }

  // position counts output digits, not input characters; colons and decimal points must not advance it.
  @SuppressWarnings("java:S1994")
  public byte[] encode(String val) {
    Arrays.fill(buf, (byte) 0);
    int position = 0;
    for (int index = 0; index < val.length() && position < 4; index++) {
      char character = val.charAt(index);
      if (character == ':') {
        buf[4] = 0x02; // Colon at HT16K33 RAM location 0x04.
      } else if (character == '.' && position > 0) {
        int previous = ((position - 1) + ((position - 1) >= 2 ? 1 : 0)) * 2;
        buf[previous] |= (byte) 0x80;
      } else {
        int ramPosition = (position + (position >= 2 ? 1 : 0)) * 2;
        buf[ramPosition] = (byte) segments(character);
        position++;
      }
    }
    return buf;
  }

  private int segments(char character) {
    if (character >= '0' && character <= '9') {
      return font[character - '0'];
    }
    return switch (character) {
      case ' ' -> 0;
      case '-' -> 0x40;
      default -> 0xff;
    };
  }

  @Override
  public String getName() {
    return "Quad LED";
  }

  @Override
  public String getDescription() {
    return "Quad 7 segment numeric LED";
  }

}