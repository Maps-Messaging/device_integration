

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

  public MeasurementBlock() {
    co2ppm = 0.0f;
    temperatureC = 0.0f;
    humidityPercent = 0.0f;
    vocIndex = 0.0f;
    noxIndex = 0.0f;
    pm1_0 = 0.0f;
    pm2_5 = 0.0f;
    pm4_0 = 0.0f;
    pm10_0 = 0.0f;
    hchoPpb = 0.0f;
  }

  public MeasurementBlock(float pm1_0, float pm2_5, float pm4_0, float pm10_0,
                           float humidity, float temperature, float vocIndex, float noxIndex,
                           float co2ppm, float hchoPpb) {
    this.pm1_0 = pm1_0;
    this.pm2_5 = pm2_5;
    this.pm4_0 = pm4_0;
    this.pm10_0 = pm10_0;
    this.humidityPercent = humidity;
    this.temperatureC = temperature;
    this.vocIndex = vocIndex;
    this.noxIndex = noxIndex;
    this.co2ppm = co2ppm;
    this.hchoPpb = hchoPpb;
  }
}
