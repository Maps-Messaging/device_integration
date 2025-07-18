

/*
 *    Copyright [ 2020 - 2024 ] Matthew Buckton
 *    Copyright [ 2024 - 2025 ] MapsMessaging B.V.
 *
 *    Licensed under the Apache License, Version 2.0 with the Commons Clause
 *    (the "License"); you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at:
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *        https://commonsclause.com/
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License
 */

package io.mapsmessaging.devices.i2c.devices.sensors.sen6x.data;


import lombok.Getter;

import java.io.IOException;

@Getter
public class MeasurementBlock {

  private final float co2ppm;
  private final float temperatureC;
  private final float humidityPercent;
  private final float vocIndex;
  private final float noxIndex;
  private final float pm1_0;
  private final float pm2_5;
  private final float pm4_0;
  private final float pm10_0;
  private final float hchoPpb;

  private MeasurementBlock(float co2, float temp, float humidity, float voc, float nox,
                           float pm1_0, float pm2_5, float pm4_0, float pm10_0,
                           float hcho) {
    this.co2ppm = co2;
    this.temperatureC = temp;
    this.humidityPercent = humidity;
    this.vocIndex = voc;
    this.noxIndex = nox;
    this.pm1_0 = pm1_0;
    this.pm2_5 = pm2_5;
    this.pm4_0 = pm4_0;
    this.pm10_0 = pm10_0;
    this.hchoPpb = hcho;
  }

  public static MeasurementBlock fromRaw(byte[] raw) throws IOException {
    if (raw.length < 27 + 3) throw new IOException("Invalid measurement block size"); // now requires 30 bytes
    return new MeasurementBlock(
        parseFloat(raw, 0),
        parseFloat(raw, 3),
        parseFloat(raw, 6),
        parseFloat(raw, 9),
        parseFloat(raw, 12),
        parseFloat(raw, 15),
        parseFloat(raw, 18),
        parseFloat(raw, 21),
        parseFloat(raw, 24),
        parseFloat(raw, 27)
    );
  }

  private static float parseFloat(byte[] raw, int offset) {
    if (offset + 1 >= raw.length) return 0f;
    int msb = raw[offset] & 0xFF;
    int lsb = raw[offset + 1] & 0xFF;
    return ((msb << 8) | lsb) * 1.0f;
  }
}
