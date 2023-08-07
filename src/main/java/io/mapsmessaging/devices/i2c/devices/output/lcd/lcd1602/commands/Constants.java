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

public class Constants {

  public static final byte CONTROL = (byte) 0b10000000;
  public static final byte RS = (byte) 0b01000000;

  public static final byte CLEAR_DISPLAY = 0x01;
  public static final byte RETURN_HOME = 0x02;
  public static final byte ENTRY_MODE_SET = 0x04;
  public static final byte DISPLAY_CONTROL = 0x08;
  public static final byte CURSOR_SHIFT = 0x10;
  public static final byte FUNCTION_SET = 0x20;
  public static final byte SET_CGRAM_ADDR = 0x40;
  public static final byte SET_DDRAM_ADDR = (byte) 0x80;
  
}
