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

public class FunctionSet extends Command {

  private static final byte FUNCTION_SET = 0b00100000;
  private static final byte SET_2_LINES = 0b00001000;
  private static final byte SET_5_BY_10 = 0b00000100;

  public FunctionSet() {
    super(CONTROL, FUNCTION_SET);
  }

  public void set5by10Font() {
    buffer[1] = (byte) (buffer[1] | SET_5_BY_10);
  }

  public void set5by8Font() {
    buffer[1] = (byte) (buffer[1] & ~SET_5_BY_10);
  }

  public void set2LineDisplay() {
    buffer[1] = (byte) (buffer[1] | SET_2_LINES);
  }

  public void set1LineDisplay() {
    buffer[1] = (byte) (buffer[1] & ~SET_2_LINES);
  }

  public int getCycleTime() {
    return 5;
  }

  public int repeatCount() {
    return 2;
  }

}
