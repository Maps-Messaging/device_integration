package io.mapsmessaging.devices.i2c.devices.rtc.ds3231;

import io.mapsmessaging.devices.sensorreadings.ReadingSupplier;
import io.mapsmessaging.devices.sensorreadings.SensorReading;

import java.time.LocalDateTime;

public class LocalDateTimeSensorReading extends SensorReading<LocalDateTime> {


  protected LocalDateTimeSensorReading(String name, String unit, ReadingSupplier<LocalDateTime> valueSupplier) {
    super(name, unit, valueSupplier);
  }
}
