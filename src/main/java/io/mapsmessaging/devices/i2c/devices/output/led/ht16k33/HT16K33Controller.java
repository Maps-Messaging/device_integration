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

package io.mapsmessaging.devices.i2c.devices.output.led.ht16k33;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.devices.output.Task;
import io.mapsmessaging.devices.i2c.devices.output.led.ht16k33.tasks.Clock;
import io.mapsmessaging.devices.i2c.devices.output.led.ht16k33.tasks.TestTask;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public abstract class HT16K33Controller extends I2CDeviceController {

  private static final String BRIGHTNESS = "brightness";
  private static final String BLINK = "blink";
  private static final String ENABLED = "enabled";
  private static final String DISPLAY = "display";
  private static final String CLOCK = "clock";
  private static final String TEST = "test";

  private static final String TASK = "task";
  private static final String RAW = "raw";


  protected final HT16K33Driver driver;

  private Task currentTask;

  protected HT16K33Controller() {
    this.driver = null;
    currentTask = null;
  }

  protected HT16K33Controller(HT16K33Driver display, AddressableDevice device) {
    super(device);
    this.driver = display;
  }

  public I2CDevice getDevice() {
    return driver;
  }

  public DeviceType getType(){
    return getDevice().getType();
  }

  @Override
  public void close() {
    cancelCurrentTask();
  }

  @Override
  public boolean detect(AddressableDevice i2cDevice) {
    return driver != null && driver.isConnected();
  }

  @Override
  public byte[] getDeviceConfiguration() {
    return "{}".getBytes();
  }

  @Override
  public byte[] getDeviceState() {
    JSONObject jsonObject = new JSONObject();
    if (driver != null) {
      jsonObject.put(DISPLAY, driver.getCurrent());
      jsonObject.put(BLINK, driver.getRate().name());
      jsonObject.put(ENABLED, driver.isOn());
      jsonObject.put(BRIGHTNESS, driver.getBrightness());
    }
    return jsonObject.toString(2).getBytes();
  }

  public void rawWrite(String value) throws IOException {
    if (driver != null) {
      driver.writeRaw(value);
    }
  }

  public void write(String value) throws IOException {
    if (driver != null) {
      driver.write(value);
    }
  }

  private void processBrightness(JSONObject jsonObject, JSONObject response) throws IOException {
    if (jsonObject.has(BRIGHTNESS)) {
      int brightness = jsonObject.getInt(BRIGHTNESS);
      if (brightness != driver.getBrightness()) {
        driver.setBrightness((byte) (brightness & 0xf));
        response.put(BRIGHTNESS, brightness);
      }
    }
  }

  private void processBlink(JSONObject jsonObject, JSONObject response) throws IOException {
    if (jsonObject.has(BLINK)) {
      String blink = jsonObject.optString(BLINK, "OFF");
      BlinkRate rate = BlinkRate.valueOf(blink);
      driver.setBlinkRate(rate);
      response.put(BLINK, rate.name());
    }
  }

  private void processEnabled(JSONObject jsonObject, JSONObject response) throws IOException {
    if (jsonObject.has(ENABLED)) {
      boolean isOn = jsonObject.optBoolean(ENABLED, driver.isOn());
      if (isOn != driver.isOn()) {
        if (isOn) {
          response.put(ENABLED, isOn);
          driver.turnOn();
        } else {
          response.put(ENABLED, isOn);
          driver.turnOff();
        }
      }
    }
  }

  private void processDisplay(JSONObject jsonObject, JSONObject response) throws IOException {
    if (jsonObject.has(DISPLAY)) {
      cancelCurrentTask();
      String text = jsonObject.getString(DISPLAY);
      if (text.length() <= 5) {
        driver.write(text);
        response.put(DISPLAY, text);
      }
    }
  }

  private void processRaw(JSONObject jsonObject, JSONObject response) throws IOException {
    if (jsonObject.has(RAW)) {
      cancelCurrentTask();
      String text = jsonObject.getString(RAW);
      driver.writeRaw(text);
      response.put(RAW, text);
    }
  }

  private void processTask(JSONObject jsonObject, JSONObject response) throws IOException {
    if (jsonObject.has(TASK)) {
      cancelCurrentTask();
      driver.write("    ");
      String task = jsonObject.getString(TASK);
      if (task.equalsIgnoreCase(CLOCK)) {
        setTask(new Clock(this));
        response.put(TASK, CLOCK);
      }
      if (task.equalsIgnoreCase(TEST)) {
        setTask(new TestTask(this));
        response.put(TASK, TEST);
      }
    }
  }

  @Override
  public byte[] updateDeviceConfiguration(byte[] val) throws IOException {
    JSONObject response = new JSONObject();
    if (driver != null) {
      JSONObject jsonObject = null;
      try {
        jsonObject = new JSONObject(new String(val));
      }
      catch (JSONException jsonException){
        //
      }
      if(jsonObject != null) {
        processBrightness(jsonObject, response);
        processBlink(jsonObject, response);
        processEnabled(jsonObject, response);
        processDisplay(jsonObject, response);
        processRaw(jsonObject, response);
        processTask(jsonObject, response);
      }
      else{
        cancelCurrentTask();
        String text = new String(val);
        driver.writeRaw(text);
        response.put(RAW, text);
      }
    }
    return response.toString(2).getBytes();
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig();
    config.setSource(getName());
    config.setVersion("1.0");
    config.setResourceType("LED");
    config.setInterfaceDescription("Controls the LED segments");
    return config;
  }

  private synchronized void setTask(Task task) {
    cancelCurrentTask();
    currentTask = task;
  }

  private synchronized void cancelCurrentTask() {
    if (currentTask != null) {
      currentTask.stop();
      currentTask = null;
    }
  }
}
