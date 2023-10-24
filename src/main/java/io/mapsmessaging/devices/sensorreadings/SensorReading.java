package io.mapsmessaging.devices.sensorreadings;

import lombok.Getter;

import java.io.IOException;


@Getter
public class SensorReading<T> {

  private final String name;

  @Getter
  private final String unit;

  private final ReadingSupplier<T> supplier;

  protected SensorReading(String name, String unit, ReadingSupplier<T> valueSupplier) {
    this.name = name;
    this.unit = unit;
    this.supplier = valueSupplier;
  }

  public ComputationResult<T> getValue() {
    try {
      return ComputationResult.success(format(supplier.get()));
    } catch (IOException ioException) {
      return ComputationResult.failure(ioException);
    }
  }

  protected T format(T val){
    return val;
  }
}
