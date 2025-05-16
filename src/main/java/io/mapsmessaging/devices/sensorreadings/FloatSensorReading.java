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

package io.mapsmessaging.devices.sensorreadings;

import lombok.Getter;

public class FloatSensorReading extends NumericSensorReading<Float> {

  @Getter
  private final int precision;

  public FloatSensorReading(String name, String unit, String description, Float example, boolean readOnly, float min, float max, int precision, ReadingSupplier<Float> valueSupplier) {
    super(name, unit, description, example, readOnly, min, max, valueSupplier);
    this.precision = precision;
  }

  public static float roundToDecimalPlaces(float value, int places) {
    float scale = (float) Math.pow(10, places);
    return Math.round(value * scale) / scale;
  }

  @Override
  protected Float format(Float val) {
    if (precision >= 0) {
      return roundToDecimalPlaces(val, precision);
    }
    return val;
  }
}
