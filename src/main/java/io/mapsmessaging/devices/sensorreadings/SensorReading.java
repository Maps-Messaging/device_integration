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

import java.io.IOException;


@Getter
public class SensorReading<T> {

  private final String name;
  private final String unit;
  private final String description;
  private final T example;
  private final boolean readOnly;

  private final ReadingSupplier<T> supplier;

  protected SensorReading(String name, String unit, String description, T example, boolean readOnly, ReadingSupplier<T> valueSupplier) {
    this.name = name;
    this.unit = unit;
    this.description = description;
    this.example = example;
    this.readOnly = readOnly;
    this.supplier = valueSupplier;
  }

  protected SensorReading(String name, String unit, ReadingSupplier<T> valueSupplier) {
    this(name, unit, null, null, true, valueSupplier);
  }

  public ComputationResult<T> getValue() {
    try {
      return ComputationResult.success(format(supplier.get()));
    } catch (IOException ioException) {
      return ComputationResult.failure(ioException);
    }
  }

  protected T format(T val) {
    return val;
  }
}
