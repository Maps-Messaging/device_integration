package io.mapsmessaging.devices.sensorreadings;

public class IntegerSensorReading extends NumericSensorReading<Integer> {

  public IntegerSensorReading(String name, String unit, int min, int max, ReadingSupplier<Integer> valueSupplier) {
    super(name, unit, min, max, valueSupplier);
  }
}