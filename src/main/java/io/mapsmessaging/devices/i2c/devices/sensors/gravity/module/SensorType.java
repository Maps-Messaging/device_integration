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

package io.mapsmessaging.devices.i2c.devices.sensors.gravity.module;

import lombok.Getter;

@Getter
public enum SensorType {

  NH3(0x2, "Ammonia", "SEN0469", 0, 100, "ppm", 0, 150, new NH3Module(), 10, "NH₃"),
  H2S(0x3, "Hydrogen Sulfide", "SEN0467", 0, 100, "ppm", 0, 30, new H2SModule(), 10, "H₂S"),
  CO(0x4, "Carbon Monoxide", "SEN0466", 0, 1000, "ppm", 0, 30, new COModule(), 50, "CO"),
  O2(0x5, "Oxygen", "SEN0465", 0, 25, "%Vol", 1, 15, new O2Module(), 195, "O₂"),
  H2(0x6, "Hydrogen", "SEN0473", 0, 1000, "ppm", 0, 120, new H2Module(), 50, "H₂"),
  O3(0x2A, "Ozone", "SEN0472", 0, 10, "ppm", 1, 120, new O3Module(), 50, "O₃"),
  SO2(0x2B, "Sulfur Dioxide", "SEN0470", 0, 20, "ppm", 1, 30, new SO2Module(), 100, "SO₂"),
  NO2(0x2C, "Nitrogen Dioxide", "SEN0471", 0, 20, "ppm", 1, 30, new NO2Module(), 50, "NO₂"),
  HCL(0x2E, "Hydrogen Chloride", "SEN0474", 0, 10, "ppm", 1, 60, new HCLModule(), 50, "HCl"),
  Cl2(0x31, "Chlorine", "SEN0468", 0, 20, "ppm", 1, 60, new Cl2Module(), 50, "Cl₂"),
  HF(0x33, "Hydrogen Fluoride", "SEN0475", 0, 10, "ppm", 1, 60, new HFModule(), 30, "HF"),
  PH3(0x45, "Phosphine", "SEN0476", 0, 1000, "ppm", 1, 30, new PH3Module(), 50, "PH₃"),
  UNKNOWN(0x0, "Unknown", "Unknown", 0, 0, "", 0, 0, null, 0, "Unknown");

  @Getter
  final String name;
  private final int type;
  @Getter
  private final String sku;
  @Getter
  private final int minimumRange;

  @Getter
  private final int maximumRange;

  @Getter
  private final String units;

  @Getter
  private final int resolution;

  @Getter
  private final int responseTime;

  @Getter
  private final SensorModule sensorModule;

  @Getter
  private final int threshold;

  private final String gasType;

  SensorType(int type,
             String name,
             String sku,
             int min,
             int max,
             String units,
             int resolution,
             int responseTime,
             SensorModule sensorModule,
             int threshold,
             String gasType) {
    this.type = type;
    this.name = name;
    this.sku = sku;
    this.minimumRange = min;
    this.maximumRange = max;
    this.units = units;
    this.resolution = resolution;
    this.responseTime = responseTime;
    this.sensorModule = sensorModule;
    this.threshold = threshold;
    this.gasType = gasType;
  }


  public static SensorType getByType(int type) {
    for (SensorType module : SensorType.values()) {
      if (module.type == type) {
        return module;
      }
    }
    return null;
  }

}
