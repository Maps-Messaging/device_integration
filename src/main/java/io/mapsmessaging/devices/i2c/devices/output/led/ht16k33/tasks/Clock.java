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
