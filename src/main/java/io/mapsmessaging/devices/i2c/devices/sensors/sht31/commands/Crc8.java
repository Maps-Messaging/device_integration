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

package io.mapsmessaging.devices.i2c.devices.sensors.sht31.commands;

public class Crc8 {
  public static byte compute(byte[] data, int offset, int length) {
    int crc = 0xFF;
    for (int i = offset; i < offset + length; i++) {
      crc ^= (data[i] & 0xFF);
      for (int b = 0; b < 8; b++) {
        crc = (crc & 0x80) != 0 ? (crc << 1) ^ 0x31 : (crc << 1);
        crc &= 0xFF;
      }
    }
    return (byte) crc;
  }

  public static boolean check(byte[] data, int offset) {
    return compute(data, offset, 2) == data[offset + 2];
  }
}
