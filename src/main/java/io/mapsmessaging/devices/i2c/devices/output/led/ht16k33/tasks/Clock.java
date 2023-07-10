package io.mapsmessaging.devices.i2c.devices.output.led.ht16k33.tasks;

import io.mapsmessaging.devices.i2c.devices.output.led.ht16k33.HT16K33Controller;
import io.mapsmessaging.devices.util.Delay;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

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
        StringBuilder val = new StringBuilder();
        LocalDateTime dateTime = LocalDateTime.now();
        int hour = dateTime.getHour();
        int min = dateTime.getMinute();
        if (hour < 10) {
          val.append("0");
        }
        val.append(hour);
        if (hasColon) {
          val.append(" ");
        } else {
          val.append(":");
        }
        hasColon = !hasColon;
        if (min < 10) {
          val.append("0");
        }
        val.append(min);
        controller.write(val.toString());
        Delay.pause(450);
      }
    } catch (IOException e) {
      // ignore since we may have lost the device
    }
  }
}
