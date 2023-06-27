package io.mapsmessaging.devices.i2c.devices.output.led.ht16k33.tasks;

import io.mapsmessaging.devices.i2c.devices.output.led.ht16k33.HT16K33Controller;
import io.mapsmessaging.devices.util.Delay;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

public class Clock implements Task {

  private final HT16K33Controller controller;
  private final AtomicBoolean runFlag;

  public Clock(HT16K33Controller controller) {
    this.controller = controller;
    runFlag = new AtomicBoolean(true);
  }

  @Override
  public void stop() {
    runFlag.set(false);
  }

  @Override
  public void run() {
    boolean hasColon = false;
    StringBuffer val = new StringBuffer();
    while (runFlag.get()) {
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
      val.append("0").append(min);
      JSONObject payload = new JSONObject();
      payload.put("display", val);
      controller.setPayload(payload.toString(2).getBytes());
      Delay.pause(450);
    }
  }
}
