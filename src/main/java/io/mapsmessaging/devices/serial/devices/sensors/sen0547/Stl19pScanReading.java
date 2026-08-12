/*
 *
 *  Copyright [ 2020 - 2024 ] Matthew Buckton
 *  Copyright [ 2024 - 2026 ] MapsMessaging B.V.
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

package io.mapsmessaging.devices.serial.devices.sensors.sen0547;

import io.mapsmessaging.devices.sensorreadings.ReadingSupplier;
import io.mapsmessaging.devices.sensorreadings.SensorReading;

public class Stl19pScanReading extends SensorReading<Stl19pSensor.Scan> {

  public Stl19pScanReading(ReadingSupplier<Stl19pSensor.Scan> supplier) {
    super("scan", "", "Complete 360 degree LiDAR scan", null, true, supplier);
  }
}
