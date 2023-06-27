package io.mapsmessaging.devices.i2c.devices.sensors.ds3231;

import io.mapsmessaging.devices.i2c.devices.sensors.ds3231.register.AlarmRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.ds3231.register.ControlRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.ds3231.register.StatusRegister;
import org.json.JSONObject;

public class JsonPacker {

  private final Ds3231Rtc rtc;

  public JsonPacker(Ds3231Rtc rtc) {
    this.rtc = rtc;
  }

  public byte[] pack() {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("date", rtc.getDate());
    jsonObject.put("time", rtc.getTime());
    jsonObject.put("alarm1", packAlarm(rtc.getAlarm1()));
    jsonObject.put("alarm2", packAlarm(rtc.getAlarm2()));
    jsonObject.put("status", packStatus(rtc.getStatusRegiser()));
    jsonObject.put("control", packControl(rtc.getControlRegister()));
    jsonObject.put("temperature", rtc.getTemperature());
    return jsonObject.toString(2).getBytes();
  }

  private JSONObject packControl(ControlRegister controlRegister) {
    JSONObject control = new JSONObject();
    control.put("covertTemperatureEnabled", controlRegister.isConvertTemperatureEnabled());
    control.put("oscillatorEnabled", controlRegister.isOscillatorEnabled());
    control.put("squareWaveEnabled", controlRegister.isSquareWaveEnabled());
    control.put("squareWaveInterruptEnabled", controlRegister.isSquareWaveInterruptEnabled());
    control.put("squareWaveFrequency", controlRegister.getSquareWaveFrequency());
    control.put("alarm1InterruptEnabled", controlRegister.isAlarm1InterruptEnabled());
    control.put("alarm2InterruptEnabled", controlRegister.isAlarm2InterruptEnabled());
    return control;
  }

  private JSONObject packStatus(StatusRegister statusRegister) {
    JSONObject status = new JSONObject();
    status.put("32khz", statusRegister.is32kHzOutputEnabled());
    status.put("alarm1Set", statusRegister.isAlarm1FlagSet());
    status.put("alarm2Set", statusRegister.isAlarm2FlagSet());
    status.put("oscillatorStopped", statusRegister.isOscillatorStopped());
    return status;
  }

  private JSONObject packAlarm(AlarmRegister alarmRegister) {
    JSONObject alarm = new JSONObject();
    alarm.put("time", alarmRegister.getTime());
    alarm.put("rate", alarmRegister.getRate().name());
    if (alarmRegister.getRate().getMask() == 0) {
      if (alarmRegister.getRate().isDayOfWeek()) {
        alarm.put("dayOfWeek", alarmRegister.getDayOrDate());
      } else {
        alarm.put("dayOfMonth", alarmRegister.getDayOrDate());
      }
    }
    return alarm;
  }

}
