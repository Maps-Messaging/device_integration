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

package io.mapsmessaging.devices.deviceinterfaces;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public interface Clock {

  LocalDate getDate() throws IOException;

  void setDate(LocalDate date) throws IOException;

  LocalTime getTime() throws IOException;

  void setTime(LocalTime time) throws IOException;

  LocalDateTime getDateTime() throws IOException;

  void setDateTime(LocalDateTime dateTime) throws IOException;

  default void setAlarm(int alarmNumber, LocalDateTime dateTime) throws IOException {
  }

  default void setAlarm(int alarmNumber, LocalTime time) throws IOException {
  }

}
