/*
 *      Copyright [ 2020 - 2023 ] [Matthew Buckton]
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package io.mapsmessaging.devices.i2c.devices.rtc.ds3231;

import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.register.AlarmRegister;
import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.register.ControlRegister;
import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.register.StatusRegister;
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
