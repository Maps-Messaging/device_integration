package io.mapsmessaging.devices.sensorreadings;

public class FloatSensorReading extends NumericSensorReading<Float> {

  public FloatSensorReading(String name, String unit, float min, float max, ReadingSupplier<Float> valueSupplier) {
    super(name, unit, min, max, valueSupplier);
  }

}
