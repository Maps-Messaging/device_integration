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

package io.mapsmessaging.devices.sensorreadings;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class GroupSensorReading extends SensorReading<Void> {

  @Getter
  private final List<SensorReading<?>> groupList;


  protected GroupSensorReading(String name, String unit, String description, Void example, boolean readOnly, ReadingSupplier<Void> valueSupplier) {
    super(name, unit, description, example, readOnly, valueSupplier);
    groupList = new ArrayList<>();
  }

  protected GroupSensorReading(String name, String unit, ReadingSupplier<Void> valueSupplier) {
    super(name, unit, valueSupplier);
    groupList = new ArrayList<>();
  }

}
