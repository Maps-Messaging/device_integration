/*
 *    Copyright [ 2020 - 2024 ] Matthew Buckton
 *    Copyright [ 2024 - 2025 ] MapsMessaging B.V.
 *
 *    Licensed under the Apache License, Version 2.0 with the Commons Clause
 *    (the "License"); you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at:
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *        https://commonsclause.com/
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License
 */

package io.mapsmessaging.devices.i2c.devices.sensors.sen6x.commands;

import io.mapsmessaging.devices.sensorreadings.FloatSensorReading;

import java.io.IOException;

public abstract class AbstractMeasurementCommand {

  protected final Sen6xMeasurementManager manager;
  private final String name;
  private final String unit;
  private final String description;
  private final float example;
  private final boolean readOnly;
  private final float min;
  private final float max;
  private final int precision;

  protected AbstractMeasurementCommand(
      Sen6xMeasurementManager manager,
      String name,
      String unit,
      String description,
      float example,
      boolean readOnly,
      float min,
      float max,
      int precision
  ) {
    this.manager = manager;
    this.name = name;
    this.unit = unit;
    this.description = description;
    this.example = example;
    this.readOnly = readOnly;
    this.min = min;
    this.max = max;
    this.precision = precision;
  }

  public abstract float getValue() throws IOException;

  public FloatSensorReading asSensorReading() {
    return new FloatSensorReading(
        name,
        unit,
        description,
        example,
        readOnly,
        min,
        max,
        precision,
        this::getValue
    );
  }
}
