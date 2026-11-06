/*
 *
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

package io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions;

import io.mapsmessaging.devices.impl.AddressableDevice;

public class ForcedRecalibrationRequest extends Request {

  public ForcedRecalibrationRequest(AddressableDevice device) {
    super(400, 0x362f, 3, device);
  }

  // The constructor fixes responseLength at three bytes; Request rejects short reads before returning.
  @SuppressWarnings("javabugs:S6466")
  public int recalibrate(int referencePpm) {
    if (referencePpm < 1 || referencePpm > 0xffff) {
      throw new IllegalArgumentException("SCD41 reference CO2 must be between 1 and 65535 ppm");
    }
    byte[] command = getCommand();
    byte[] payload = {
        command[0], command[1],
        (byte) (referencePpm >>> 8), (byte) referencePpm, 0
    };
    payload[4] = generateCrc(payload, 2);
    byte[] response = sendAndWaitForResponse(payload);
    if (generateCrc(response, 0) != response[2]) {
      throw new IllegalStateException("Invalid SCD41 recalibration response CRC");
    }
    int correction = ((response[0] & 0xff) << 8) | (response[1] & 0xff);
    if (correction == 0xffff) {
      throw new IllegalStateException("SCD41 forced recalibration failed");
    }
    return correction - 0x8000;
  }
}
