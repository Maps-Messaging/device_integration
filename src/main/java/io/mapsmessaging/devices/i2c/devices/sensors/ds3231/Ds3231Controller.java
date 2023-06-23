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

package io.mapsmessaging.devices.i2c.devices.sensors.ds3231;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.i2c.I2CDeviceEntry;
import io.mapsmessaging.devices.i2c.devices.sensors.ds3231.register.AlarmRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.ds3231.register.ControlRegister;
import io.mapsmessaging.devices.i2c.devices.sensors.ds3231.register.StatusRegister;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import lombok.Getter;
import org.json.JSONObject;

public class Ds3231Controller implements I2CDeviceEntry {

  private final int i2cAddr = 0x68;
  private final Ds3231Rtc rtc;

  @Getter
  private final String name = "DS3231";


  public Ds3231Controller() {
    rtc = null;
  }

  public Ds3231Controller(I2C device) {
    rtc = new Ds3231Rtc(device);
  }

  @Override
  public boolean detect() {
    return rtc != null && rtc.isConnected();
  }

  public I2CDeviceEntry mount(I2C device) {
    return new Ds3231Controller(device);
  }

  public byte[] getStaticPayload() {
    JSONObject jsonObject = new JSONObject();
    if (rtc != null) {

    }
    return jsonObject.toString(2).getBytes();
  }

  public byte[] getUpdatePayload() {
    JSONObject jsonObject = new JSONObject();
    if (rtc != null) {
      rtc.read();
      jsonObject.put("date", rtc.getDate());
      jsonObject.put("time", rtc.getTime());
      jsonObject.put("alarm1", packAlarm(rtc.getAlarm1()));
      jsonObject.put("alarm2", packAlarm(rtc.getAlarm2()));
      jsonObject.put("status", packStatus(rtc.getStatusRegiser()));
      jsonObject.put("control", packControl(rtc.getControlRegister()));
      jsonObject.put("temperature",rtc.getTemperature());
    }
    return jsonObject.toString(2).getBytes();
  }

  private JSONObject packControl(ControlRegister controlRegister){
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

  private JSONObject packStatus(StatusRegister statusRegister){
    JSONObject status = new JSONObject();
    status.put("32khz", statusRegister.is32kHzOutputEnabled());
    status.put("alarm1Set", statusRegister.isAlarm1FlagSet());
    status.put("alarm2Set", statusRegister.isAlarm2FlagSet());
    status.put("oscillatorStopped", statusRegister.isOscillatorStopped());
    return status;
  }

  private JSONObject packAlarm(AlarmRegister alarmRegister){
    JSONObject alarm = new JSONObject();
    alarm.put("time", alarmRegister.getTime());
    alarm.put("rate", alarmRegister.getRate().name());
    if(alarmRegister.getRate().getMask() == 0) {
      if(alarmRegister.getRate().isDayOfWeek()){
        alarm.put("dayOfWeek", alarmRegister.getDayOrDate());
      }
      else{
        alarm.put("dayOfMonth", alarmRegister.getDayOrDate());
      }
    }
    return alarm;
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig();
    config.setComments("i2c RTC");
    config.setSource("I2C bus address : 0x68");
    config.setVersion("1.0");
    config.setResourceType("rtc");
    config.setInterfaceDescription("Returns JSON object containing the latest rtc");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    return new int[]{i2cAddr};
  }
}