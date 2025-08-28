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
import io.mapsmessaging.devices.i2c.devices.output.led.ht16k33.Panel;
import io.mapsmessaging.devices.i2c.devices.output.led.ht16k33.SevenSegmentLed;
import io.mapsmessaging.devices.util.Delay;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestTask implements Task {

  private final HT16K33Controller controller;
  private final AtomicBoolean runFlag;

  public TestTask(HT16K33Controller controller) {
    this.controller = controller;
    runFlag = new AtomicBoolean(true);
    Thread thread = new Thread(this);
    thread.start();
  }

  @Override
  public void stop() {
    runFlag.set(false);
  }

  private void displayMask(Panel panel, int mask, long delay) throws IOException {
    panel.setAllDisplay(mask);
    controller.rawWrite(panel.pack());
    Delay.pause(delay);

  }

  private void marchUpDown(Panel panel) throws IOException {
    for (int x = 0; x < 10; x++) {
      displayMask(panel, SevenSegmentLed.BOTTOM.getMask(), 200);
      displayMask(panel, SevenSegmentLed.MIDDLE.getMask(), 200);
      displayMask(panel, SevenSegmentLed.TOP.getMask(), 200);
      displayMask(panel, SevenSegmentLed.MIDDLE.getMask(), 200);
      displayMask(panel, SevenSegmentLed.BOTTOM.getMask(), 200);
    }
  }

  private void marchLeftRight(Panel panel) throws IOException {
    for (int x = 0; x < 4; x++) {
      panel.setDisplay(x, SevenSegmentLed.TOP_LEFT.getMask() | SevenSegmentLed.BOTTOM_LEFT.getMask());
      controller.rawWrite(panel.pack());
      if (!runFlag.get()) return;
      Delay.pause(200);
      panel.clear();
      panel.setDisplay(x, SevenSegmentLed.TOP_RIGHT.getMask() | SevenSegmentLed.BOTTOM_RIGHT.getMask());
      controller.rawWrite(panel.pack());
      if (!runFlag.get()) return;
      Delay.pause(200);
      panel.clear();
    }
    for (int x = 3; x >= 0; x--) {
      panel.setDisplay(x, SevenSegmentLed.TOP_RIGHT.getMask() | SevenSegmentLed.BOTTOM_RIGHT.getMask());
      controller.rawWrite(panel.pack());
      if (!runFlag.get()) return;
      Delay.pause(200);
      panel.clear();
      panel.setDisplay(x, SevenSegmentLed.TOP_LEFT.getMask() | SevenSegmentLed.BOTTOM_LEFT.getMask());
      controller.rawWrite(panel.pack());
      if (!runFlag.get()) return;
      Delay.pause(200);
      panel.clear();
    }
  }

  private void circle(Panel panel) throws IOException {
    for (int x = 0; x < 40; x++) {
      panel.setDisplay(x, SevenSegmentLed.TOP_LEFT.getMask());
      panel.setDisplay(3 - x, SevenSegmentLed.BOTTOM_RIGHT.getMask());
      controller.rawWrite(panel.pack());
      if (!runFlag.get()) return;
      Delay.pause(200);
      panel.clear();
      panel.setDisplay(x, SevenSegmentLed.TOP_RIGHT.getMask());
      panel.setDisplay(3 - x, SevenSegmentLed.BOTTOM_LEFT.getMask());
      controller.rawWrite(panel.pack());
      if (!runFlag.get()) return;
      Delay.pause(200);
      panel.clear();
    }
  }

  private void displayDecimal(Panel panel) throws IOException {
    for (int x = 0; x < 4; x++) {
      panel.setDisplay(x, SevenSegmentLed.DECIMAL.getMask());
      controller.rawWrite(panel.pack());
      if (!runFlag.get()) return;
      Delay.pause(200);
      panel.clear();
    }
  }

  private void blinkColon(Panel panel) throws IOException {
    for (int x = 0; x < 4; x++) {
      panel.enableColon(true);
      controller.rawWrite(panel.pack());
      if (!runFlag.get()) return;
      Delay.pause(200);
      panel.enableColon(false);
      controller.rawWrite(panel.pack());
      if (!runFlag.get()) return;
      Delay.pause(200);
    }
  }

  @Override
  public void run() {
    Panel panel = new Panel(4, true);
    boolean hasColon = false;
    try {
      while (runFlag.get()) {
        panel.enableColon(hasColon);
        hasColon = !hasColon;
        marchUpDown(panel);
        panel.clear();
        for (int y = 0; y < 10; y++) {
          marchLeftRight(panel);
        }
        panel.clear();
        circle(panel);
        panel.clear();
        displayDecimal(panel);
        panel.clear();
        blinkColon(panel);
        panel.clear();
      }
    } catch (IOException e) {
      int bus = controller.getDevice().getBus();
      DeviceBusManager.getInstance().getI2cBusManager()[bus].close(controller);
      // ignore since we have exited now
    }
  }
}
