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

package io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions;

import io.mapsmessaging.devices.impl.AddressableDevice;

public class SerialNumberRequest extends Request {
  public SerialNumberRequest(AddressableDevice device) {
    super(1, 0x3682, 9, device);
  }

  public int getSerialNumber() {
    byte[] response = getResponse();
    int val = 0;
    if (generateCrc(response, 0) == response[2]) {
      val = response[0] << 8 | (response[1] & 0xff);
    }
    if (generateCrc(response, 3) == response[5]) {
      int raw = (response[3] & 0xFF) << 8 | (response[4] & 0xFF);
      val = val << 16 | raw & 0xffff;
    }
    if (generateCrc(response, 6) == response[8]) {
      int raw = response[6] << 8 | (response[7] & 0xff);
      val = val << 16 | raw & 0xffff;
    }
    return val;
  }
}
