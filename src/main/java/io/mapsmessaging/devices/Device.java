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

package io.mapsmessaging.devices;

import io.mapsmessaging.devices.sensorreadings.SensorReading;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.mapsmessaging.devices.util.SensorReadingAugmentor.addComputedReadings;

public interface Device {

  String getName();

  String getDescription();

  DeviceType getType();

  default void delay(int ms) {
    try {
      TimeUnit.MILLISECONDS.sleep(ms);
    } catch (InterruptedException e) {
      // Ignore the interrupt
      Thread.currentThread().interrupt(); // Pass it up
    }
  }

  default String dump(byte[] buffer, int len) {
    int end = Math.min(buffer.length, len);
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (int x = 0; x < end; x++) {
      if (!first) {
        sb.append(",");
      } else {
        first = false;
      }
      sb.append(String.format("%02X", buffer[x] & 0xff));
    }
    return sb.toString();
  }


  default List<SensorReading<?>> generateSensorReadings(List<SensorReading<?>> list) {
    return addComputedReadings(list);
  }

}
