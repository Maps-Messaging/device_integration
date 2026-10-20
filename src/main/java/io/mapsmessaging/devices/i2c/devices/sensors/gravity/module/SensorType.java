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

package io.mapsmessaging.devices.i2c.devices.sensors.gravity.module;

import lombok.Getter;

@Getter
public enum SensorType {

  NH3(0x2, "Ammonia", "SEN0469", new MeasurementSpec(0, 100, "ppm", 0, 150, 10), new NH3Module(), "NH₃"),
  H2S(0x3, "Hydrogen Sulfide", "SEN0467", new MeasurementSpec(0, 100, "ppm", 0, 30, 10), new H2SModule(), "H₂S"),
  CO(0x4, "Carbon Monoxide", "SEN0466", new MeasurementSpec(0, 1000, "ppm", 0, 30, 50), new COModule(), "CO"),
  O2(0x5, "Oxygen", "SEN0465", new MeasurementSpec(0, 25, "%Vol", 1, 15, 195), new O2Module(), "O₂"),
  H2(0x6, "Hydrogen", "SEN0473", new MeasurementSpec(0, 1000, "ppm", 0, 120, 50), new H2Module(), "H₂"),
  O3(0x2A, "Ozone", "SEN0472", new MeasurementSpec(0, 10, "ppm", 1, 120, 50), new O3Module(), "O₃"),
  SO2(0x2B, "Sulfur Dioxide", "SEN0470", new MeasurementSpec(0, 20, "ppm", 1, 30, 100), new SO2Module(), "SO₂"),
  NO2(0x2C, "Nitrogen Dioxide", "SEN0471", new MeasurementSpec(0, 20, "ppm", 1, 30, 50), new NO2Module(), "NO₂"),
  HCL(0x2E, "Hydrogen Chloride", "SEN0474", new MeasurementSpec(0, 10, "ppm", 1, 60, 50), new HCLModule(), "HCl"),
  Cl2(0x31, "Chlorine", "SEN0468", new MeasurementSpec(0, 20, "ppm", 1, 60, 50), new Cl2Module(), "Cl₂"),
  HF(0x33, "Hydrogen Fluoride", "SEN0475", new MeasurementSpec(0, 10, "ppm", 1, 60, 30), new HFModule(), "HF"),
  PH3(0x45, "Phosphine", "SEN0476", new MeasurementSpec(0, 1000, "ppm", 1, 30, 50), new PH3Module(), "PH₃"),
  UNKNOWN(0x0, Labels.UNKNOWN, Labels.UNKNOWN, new MeasurementSpec(0, 0, "", 0, 0, 0), null, Labels.UNKNOWN);

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

  private record MeasurementSpec(int minimum, int maximum, String units,
                                 int resolution, int responseTime, int threshold) { }

  private static final class Labels {
    private static final String UNKNOWN = "Unknown";

    private Labels() {
      // Constants shared by enum initializers.
    }
  }

  SensorType(int type, String name, String sku, MeasurementSpec measurement,
             SensorModule sensorModule, String gasType) {
    this.type = type;
    this.name = name;
    this.sku = sku;
    this.minimumRange = measurement.minimum();
    this.maximumRange = measurement.maximum();
    this.units = measurement.units();
    this.resolution = measurement.resolution();
    this.responseTime = measurement.responseTime();
    this.sensorModule = sensorModule;
    this.threshold = measurement.threshold();
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
