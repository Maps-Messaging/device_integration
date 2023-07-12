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
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;

public class JsonUnpacker {

  private final Ds3231Rtc rtc;

  public JsonUnpacker(Ds3231Rtc rtc) {
    this.rtc = rtc;
  }

  public byte[] unpack(JSONObject jsonObject) throws IOException {
    JSONObject response = new JSONObject();
    if (jsonObject.has("date")) {
      LocalDate date = LocalDate.parse(jsonObject.getString("date"));
      rtc.setDate(date);
      response.put("date", date.toString());
    }
    if (jsonObject.has("time")) {
      LocalTime time = LocalTime.parse(jsonObject.getString("time"));
      rtc.setTime(time);
      response.put("time", time.toString());
    }
    if (jsonObject.has("alarm1")) {
      JSONObject alarm1Json = jsonObject.getJSONObject("alarm1");
      response.put("alarm1", unpackAlarm(alarm1Json, rtc.getAlarm1()));
    }
    if (jsonObject.has("alarm2")) {
      JSONObject alarm2Json = jsonObject.getJSONObject("alarm2");
      response.put("alarm2", unpackAlarm(alarm2Json, rtc.getAlarm2()));
    }
    if (jsonObject.has("control")) {
      JSONObject controlJson = jsonObject.getJSONObject("control");
      response.put("control", unpackControl(controlJson, rtc.getControlRegister()));
    }
    return response.toString(2).getBytes();
  }

  private JSONObject unpackAlarm(JSONObject alarmJson, AlarmRegister alarmRegister) throws IOException {
    String time = alarmJson.getString("time");
    String rate = alarmJson.getString("rate");
    int dayOfWeek = alarmJson.optInt("dayOfWeek", -1);
    int dayOfMonth = alarmJson.optInt("dayOfMonth", -1);

    JSONObject response = new JSONObject();
    response.put("time", time);
    response.put("rate", rate);

    AlarmRegister.RATE alarmRate = AlarmRegister.RATE.valueOf(rate);

    alarmRegister.setTime(LocalTime.parse(time));
    alarmRegister.setRate(alarmRate);

    if (alarmRate.getMask() == 0) {
      if (alarmRate.isDayOfWeek()) {
        alarmRegister.setDayOrDate(dayOfWeek);
        response.put("datOfWeek", dayOfWeek);
      } else {
        alarmRegister.setDayOrDate(dayOfMonth);
        response.put("dayOfMonth", dayOfMonth);
      }
    }
    return response;
  }


  private JSONObject unpackControl(JSONObject controlJson, ControlRegister controlRegister) throws IOException {
    boolean covertTemperatureEnabled = controlJson.getBoolean("covertTemperatureEnabled");
    boolean oscillatorEnabled = controlJson.getBoolean("oscillatorEnabled");
    boolean squareWaveEnabled = controlJson.getBoolean("squareWaveEnabled");
    boolean squareWaveInterruptEnabled = controlJson.getBoolean("squareWaveInterruptEnabled");
    int squareWaveFrequency = controlJson.getInt("squareWaveFrequency");
    boolean alarm1InterruptEnabled = controlJson.getBoolean("alarm1InterruptEnabled");
    boolean alarm2InterruptEnabled = controlJson.getBoolean("alarm2InterruptEnabled");


    controlRegister.setAlarm1InterruptEnabled(alarm1InterruptEnabled);
    controlRegister.setAlarm2InterruptEnabled(alarm2InterruptEnabled);
    controlRegister.setConvertTemperature(covertTemperatureEnabled);
    controlRegister.setOscillatorEnabled(oscillatorEnabled);
    controlRegister.setSquareWaveEnabled(squareWaveEnabled);
    controlRegister.setSquareWaveFrequency(squareWaveFrequency);
    controlRegister.setSquareWaveInterruptEnabled(squareWaveInterruptEnabled);
    return controlJson;
  }

}
