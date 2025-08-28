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

package io.mapsmessaging.devices.i2c.devices.output.led.ht16k33.tasks;

import io.mapsmessaging.devices.DeviceBusManager;
import io.mapsmessaging.devices.i2c.devices.output.Task;
import io.mapsmessaging.devices.i2c.devices.output.led.ht16k33.HT16K33Controller;
import io.mapsmessaging.devices.util.Delay;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.mapsmessaging.devices.i2c.devices.output.TimeHelper.getTime;

public class Clock implements Task {

  private final HT16K33Controller controller;
  private final AtomicBoolean runFlag;

  public Clock(HT16K33Controller controller) {
    this.controller = controller;
    runFlag = new AtomicBoolean(true);
    Thread thread = new Thread(this);
    thread.start();
  }

  @Override
  public void stop() {
    runFlag.set(false);
  }

  @Override
  public void run() {
    boolean hasColon = false;
    try {
      while (runFlag.get()) {
        controller.write(getTime(hasColon, false));
        hasColon = !hasColon;
        Delay.pause(450);
      }
    } catch (IOException e) {
      int bus = controller.getDevice().getBus();
      DeviceBusManager.getInstance().getI2cBusManager()[bus].close(controller);
      // ignore since we may have lost the device
    }
  }
}
