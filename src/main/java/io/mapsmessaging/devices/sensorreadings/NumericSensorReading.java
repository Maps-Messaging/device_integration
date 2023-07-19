package io.mapsmessaging.devices.sensorreadings;

import lombok.Getter;

public abstract class NumericSensorReading<T> extends SensorReading<T> {


  @Getter
  private final T minimum;

  @Getter
  private final T maximum;


  protected NumericSensorReading(String name, String unit, T min, T max, ReadingSupplier<T> valueSupplier) {
    super(name, unit, valueSupplier);
    this.maximum = max;
    this.minimum = min;
  }

}
