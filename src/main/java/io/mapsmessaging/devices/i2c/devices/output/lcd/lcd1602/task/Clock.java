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

package io.mapsmessaging.devices.i2c.devices.output.lcd.lcd1602.task;

import io.mapsmessaging.devices.i2c.I2CDeviceScheduler;
import io.mapsmessaging.devices.i2c.devices.output.Task;
import io.mapsmessaging.devices.i2c.devices.output.lcd.lcd1602.Lcd1602Controller;
import io.mapsmessaging.devices.i2c.devices.output.lcd.lcd1602.Lcd1602Device;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.mapsmessaging.devices.i2c.devices.output.TimeHelper.getTime;

public class Clock implements Task {

  private final AtomicBoolean runFlag;
  private final Lcd1602Device display;

  public Clock(Lcd1602Controller controller) {
    runFlag = new AtomicBoolean(true);
    display = (Lcd1602Device) controller.getDevice();
  }

  @Override
  public void stop() {
    runFlag.set(false);
  }

  @Override
  public void run() {
    synchronized (I2CDeviceScheduler.getI2cBusLock()) {
      display.clearDisplay();
      LocalDate date = LocalDate.now();
      while (runFlag.get()) {
        display.setCursor((byte) 0, (byte) 0);
        display.setDisplay(date.toString());
        for (int x = 0; x < 60; x++) {
          String time = getTime(true, true);
          display.setCursor((byte) 1, (byte) 0);
          display.setDisplay(time);
          display.delay(500);
          display.setCursor((byte) 1, (byte) 2);
          display.setDisplay(":");
          display.setCursor((byte) 1, (byte) 5);
          display.setDisplay(":");
          display.delay(500);
        }
      }
    }
  }
}
