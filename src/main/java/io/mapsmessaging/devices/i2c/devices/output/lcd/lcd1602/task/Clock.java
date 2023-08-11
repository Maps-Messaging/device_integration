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
