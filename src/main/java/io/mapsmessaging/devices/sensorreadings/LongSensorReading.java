package io.mapsmessaging.devices.sensorreadings;

public class LongSensorReading extends NumericSensorReading<Long> {

  public LongSensorReading(String name, String unit, long min, long max, ReadingSupplier<Long> valueSupplier) {
    super(name, unit, min, max, valueSupplier);
  }
}