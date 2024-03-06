package io.mapsmessaging.devices.i2c.devices.debug;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.devices.sensorreadings.LongSensorReading;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import io.mapsmessaging.devices.sensorreadings.StringSensorReading;
import io.mapsmessaging.logging.LoggerFactory;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class i2cDebugDevice extends I2CDevice implements Sensor {

  private final List<SensorReading<?>> readings;

  public i2cDebugDevice(AddressableDevice device) {
    super(device, LoggerFactory.getLogger(i2cDebugDevice.class));
    LongSensorReading millisecondReading = new LongSensorReading("milliseconds", "ms", 0, Long.MAX_VALUE,  System::currentTimeMillis);
    LongSensorReading nanosecondReading = new LongSensorReading("nanoseconds", "ns", 0, Long.MAX_VALUE,  System::nanoTime);

    StringSensorReading dateTime = new StringSensorReading("date", "", () -> LocalDateTime.now().toString());
    readings = List.of(millisecondReading, dateTime, nanosecondReading);
  }

  @Override
  public String getName() {
    return "DebugDevice";
  }

  @Override
  public String getDescription() {
    return "Simple debug time sensor";
  }

  @Override
  public boolean isConnected() {
    return true;
  }

  @Override
  public DeviceType getType() {
    return DeviceType.SENSOR;
  }

}