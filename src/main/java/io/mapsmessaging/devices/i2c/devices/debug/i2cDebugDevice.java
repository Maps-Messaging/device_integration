/*
 *
 *  Copyright [ 2020 - 2024 ] Matthew Buckton
 *  Copyright [ 2024 - 2025 ] MapsMessaging B.V.
 *
 *  Licensed under the Apache License, Version 2.0 with the Commons Clause
 *  (the "License"); you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      https://commonsclause.com/
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */

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

    LongSensorReading millisecondReading = new LongSensorReading(
        "milliseconds",
        "ms",
        "Current system time in milliseconds since epoch",
        System.currentTimeMillis(),
        true,
        0L,
        Long.MAX_VALUE,
        System::currentTimeMillis
    );

    LongSensorReading nanosecondReading = new LongSensorReading(
        "nanoseconds",
        "ns",
        "System nanosecond counter (not wall time)",
        System.nanoTime(),
        true,
        0L,
        Long.MAX_VALUE,
        System::nanoTime
    );

    StringSensorReading dateTime = new StringSensorReading(
        "date",
        "",
        "Current local date and time",
        LocalDateTime.now().toString(),
        true,
        () -> LocalDateTime.now().toString()
    );

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