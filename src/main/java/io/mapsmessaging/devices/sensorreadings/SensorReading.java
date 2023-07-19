package io.mapsmessaging.devices.sensorreadings;

import lombok.Getter;

import java.io.IOException;


public class SensorReading<T> {

  @Getter
  private final String name;

  @Getter
  private final String unit;

  private final ReadingSupplier<T> supplier;

  protected SensorReading(String name, String unit, ReadingSupplier<T> valueSupplier) {
    this.name =name;
    this.unit = unit;
    this.supplier = valueSupplier;
  }

  public ComputationResult<T> getValue() {
    try{
      return ComputationResult.success(supplier.get());
    }
    catch(IOException ioException){
      return ComputationResult.failure(ioException);
    }
  }
}
