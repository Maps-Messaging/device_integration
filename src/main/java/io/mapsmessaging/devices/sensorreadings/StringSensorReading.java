package io.mapsmessaging.devices.sensorreadings;

public class StringSensorReading extends SensorReading<String> {

  public StringSensorReading(String name, String unit, ReadingSupplier<String> valueSupplier) {
    super(name, unit, valueSupplier);
  }

}
