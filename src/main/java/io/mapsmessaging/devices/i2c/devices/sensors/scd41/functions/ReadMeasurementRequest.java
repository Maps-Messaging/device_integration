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
import lombok.Getter;

@Getter
public class ReadMeasurementRequest extends Request {

  private static final int MASK = 65535;
  private int co2;
  private float temperature;
  private float humidity;

  public ReadMeasurementRequest(AddressableDevice device) {
    super(1, 0xEC05, 9, device);
  }

  private static float computeTemperature(byte high, byte low) {
    float raw = (high & 0xFF) << 8 | (low & 0xFF);
    return -45.0f + (175.0f * (raw / MASK));
  }

  @Override
  public byte[] getResponse() {
    co2 = Integer.MIN_VALUE;
    temperature = Float.NaN;
    humidity = Float.NaN;
    byte[] response = super.getResponse();
    if (generateCrc(response, 0) == response[2]) {
      co2 = (response[0] & 0xff) << 8 | (response[1] & 0xff);
    }
    if (generateCrc(response, 3) == response[5]) {
      temperature = computeTemperature(response[3], response[4]);
    }
    if (generateCrc(response, 6) == response[8]) {
      humidity = computeHumidity(response[6], response[7]);
    }
    return response;
  }

  private float computeHumidity(byte high, byte low) {
    float raw = (high & 0xFF) << 8 | (low & 0xFF);
    return 100.0f * (raw / MASK);
  }
}
