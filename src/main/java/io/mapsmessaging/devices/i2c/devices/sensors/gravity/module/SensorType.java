package io.mapsmessaging.devices.i2c.devices.sensors.gravity.module;

import lombok.Getter;

@Getter
public enum SensorType {

  NH3(0x2, "SEN0469", 0, 100, "ppm", 1, 150, new NH3Module(), 10),
  H2S(0x3, "SEN0467", 0, 100, "ppm", 1.0f, 30, new H2SModule(), 10),
  CO(0x4, "SEN0466", 0, 1000, "ppm", 1.0f, 30, new COModule(), 50),
  O2(0x5, "SEN0465", 0, 25, "%Vol", 0.1f, 15, new O2Module(), 195),
  H2(0x6, "SEN0473", 0, 1000, "ppm", 1, 120, new H2Module(), 50),
  O3(0x2A, "SEN0472", 0, 10, "ppm", 0.1f, 120, new O3Module(), 50),
  SO2(0x2B, "SEN0470", 0, 20, "ppm", 0.1f, 30, new SO2Module(), 100),
  NO2(0x2C, "SEN0471", 0, 20, "ppm", 0.1f, 30, new NO2Module(), 50),
  HCL(0x2E, "SEN0474", 0, 10, "ppm", 0.1f, 60, new HCLModule(), 50),
  Cl2(0x31, "SEN0468", 0, 20, "ppm", 0.1f, 60, new Cl2Module(), 50),
  HF(0x33, "SEN0475", 0, 10, "ppm", 0.1f, 60, new HFModule(), 30),
  PH3(0x45, "SEN0476", 0, 1000, "ppm", 0.1f, 30, new PH3Module(), 50),
  UNKNOWN(0x0, "Unknown", 0, 0, "", 0.0f, 0, null, 0);

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
  private final float resolution;

  @Getter
  private final int responseTime;

  @Getter
  private final SensorModule sensorModule;

  @Getter
  private final int threshold;

  SensorType(int type,
             String sku,
             int min,
             int max,
             String units,
             float resolution,
             int responseTime, SensorModule sensorModule,
             int threshold) {
    this.type = type;
    this.sku = sku;
    this.minimumRange = min;
    this.maximumRange = max;
    this.units = units;
    this.resolution = resolution;
    this.responseTime = responseTime;
    this.sensorModule = sensorModule;
    this.threshold = threshold;
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
