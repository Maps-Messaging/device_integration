/*
 *
 *  Copyright [ 2020 - 2024 ] [Matthew Buckton]
 *  Copyright [ 2024 - 2025.  ] [Maps Messaging B.V.]
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package io.mapsmessaging.devices.sensorreadings;

import lombok.Getter;

@Getter
public abstract class NumericSensorReading<T extends Number> extends SensorReading<T> {

  private final T minimum;
  private final T maximum;

  protected NumericSensorReading(String name, String unit, String description, T example, boolean readOnly, T min, T max, ReadingSupplier<T> valueSupplier) {
    super(name, unit, description, example, readOnly, valueSupplier);
    this.minimum = min;
    this.maximum = max;
  }
}
