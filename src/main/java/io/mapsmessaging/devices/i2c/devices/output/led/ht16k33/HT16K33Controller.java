/*
 *
 *  Copyright [ 2020 - 2024 ] Matthew Buckton
 *  Copyright [ 2024 - 2026 ] MapsMessaging B.V.
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

package io.mapsmessaging.devices.i2c.devices.output.led.ht16k33;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.devices.output.Task;
import io.mapsmessaging.devices.i2c.devices.output.led.ht16k33.tasks.Clock;
import io.mapsmessaging.devices.i2c.devices.output.led.ht16k33.tasks.TestTask;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public abstract class HT16K33Controller extends I2CDeviceController {

  private static final String OBJECT = "object";
  private static final String PROPERTIES = "properties";
  private static final String STRING = "string";

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

  public DeviceType getType() {
    return getDevice().getType();
  }

  @Override
  public void close() {
    try {
      cancelCurrentTask();
    } finally {
      super.close();
    }
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
    JsonObject jsonObject = new JsonObject();
    if (driver != null) {
      jsonObject.addProperty(DISPLAY, driver.getCurrent());
      jsonObject.addProperty(BLINK, driver.getRate().name());
      jsonObject.addProperty(ENABLED, driver.isOn());
      jsonObject.addProperty(BRIGHTNESS, driver.getBrightness());
    }
    return gson.toJson(jsonObject).getBytes(StandardCharsets.UTF_8);
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
  @Override
  public byte[] updateDeviceConfiguration(byte[] val) throws IOException {
    JsonObject response = new JsonObject();
    if (driver != null) {
      JsonObject jsonObject = null;
      try {
        jsonObject = JsonParser.parseString(new String(val, StandardCharsets.UTF_8)).getAsJsonObject();
      } catch (Exception ignore) {
        // fallback below
      }

      if (jsonObject != null) {
        JsonObject settings = jsonObject.has("deviceStatic") && jsonObject.get("deviceStatic").isJsonObject()
            ? jsonObject.getAsJsonObject("deviceStatic") : jsonObject;
        JsonObject write = jsonObject.has("deviceWrite") && jsonObject.get("deviceWrite").isJsonObject()
            ? jsonObject.getAsJsonObject("deviceWrite") : jsonObject;
        processBrightness(settings, response);
        processBlink(settings, response);
        processEnabled(settings, response);
        processDisplay(write, response);
        processRaw(write, response);
        processTask(write, response);
      } else {
        cancelCurrentTask();
        String text = new String(val, StandardCharsets.UTF_8);
        driver.writeRaw(text);
        response.addProperty(RAW, text);
      }
    }
    return gson.toJson(response).getBytes(StandardCharsets.UTF_8);
  }

  private void processBrightness(JsonObject jsonObject, JsonObject response) throws IOException {
    if (jsonObject.has(BRIGHTNESS) && jsonObject.get(BRIGHTNESS).isJsonPrimitive()) {
      int brightness = jsonObject.get(BRIGHTNESS).getAsInt();
      if (brightness != driver.getBrightness()) {
        driver.setBrightness((byte) (brightness & 0xf));
        response.addProperty(BRIGHTNESS, brightness);
      }
    }
  }

  private void processBlink(JsonObject jsonObject, JsonObject response) throws IOException {
    if (jsonObject.has(BLINK)) {
      String blink = jsonObject.get(BLINK).getAsString();
      BlinkRate rate = BlinkRate.valueOf(blink);
      driver.setBlinkRate(rate);
      response.addProperty(BLINK, rate.name());
    }
  }

  private void processEnabled(JsonObject jsonObject, JsonObject response) throws IOException {
    if (jsonObject.has(ENABLED)) {
      boolean isOn = jsonObject.get(ENABLED).getAsBoolean();
      if (isOn != driver.isOn()) {
        response.addProperty(ENABLED, isOn);
        if (isOn) driver.turnOn();
        else driver.turnOff();
      }
    }
  }

  private void processDisplay(JsonObject jsonObject, JsonObject response) throws IOException {
    if (jsonObject.has(DISPLAY)) {
      cancelCurrentTask();
      String text = jsonObject.get(DISPLAY).getAsString();
      if (text.length() <= 5) {
        driver.write(text);
        response.addProperty(DISPLAY, text);
      }
    }
  }

  private void processRaw(JsonObject jsonObject, JsonObject response) throws IOException {
    if (jsonObject.has(RAW)) {
      cancelCurrentTask();
      String text = jsonObject.get(RAW).getAsString();
      driver.writeRaw(text);
      response.addProperty(RAW, text);
    }
  }

  private void processTask(JsonObject jsonObject, JsonObject response) throws IOException {
    if (jsonObject.has(TASK)) {
      cancelCurrentTask();
      driver.write("    ");
      String task = jsonObject.get(TASK).getAsString();
      if (CLOCK.equalsIgnoreCase(task)) {
        setTask(new Clock(this));
        response.addProperty(TASK, CLOCK);
      }
      if (TEST.equalsIgnoreCase(task)) {
        setTask(new TestTask(this));
        response.addProperty(TASK, TEST);
      }
    }
  }

  public SchemaConfig getSchema(String schema) {
    JsonSchemaConfig config = new JsonSchemaConfig(schema);
    config.setTitle(getName());
    config.setVersion(1);
    config.setResourceType("LED");
    config.setUniqueId(getSchemaId());
    config.setDescription("Controls the LED segments");
    return config;
  }

  protected String buildSchema(String title, String description) {
    JsonObject configSchema = new JsonObject();
    configSchema.addProperty("type", OBJECT);

    JsonObject configProps = new JsonObject();
    JsonObject brightness = property("integer", "Brightness level (0–15)");
    brightness.addProperty("minimum", 0);
    brightness.addProperty("maximum", 15);
    configProps.add(BRIGHTNESS, brightness);
    JsonObject blink = property(STRING, "Blink rate");
    JsonArray blinkRates = new JsonArray();
    for (BlinkRate rate : BlinkRate.values()) {
      blinkRates.add(rate.name());
    }
    blink.add("enum", blinkRates);
    configProps.add(BLINK, blink);
    configProps.add(ENABLED, property("boolean", "Turn the display on or off"));
    configSchema.add(PROPERTIES, configProps);

    JsonObject writeSchema = new JsonObject();
    writeSchema.addProperty("type", OBJECT);

    JsonObject writeProps = new JsonObject();
    JsonObject displayText = property(STRING, "Up to five input characters, including optional colon or decimal point");
    displayText.addProperty("maxLength", 5);
    writeProps.add(DISPLAY, displayText);
    JsonObject raw = property(STRING, "Raw segment bytes encoded as Base64");
    raw.addProperty("contentEncoding", "base64");
    writeProps.add(RAW, raw);
    JsonObject task = property(STRING, "Task to run");
    JsonArray tasks = new JsonArray();
    tasks.add("CLOCK");
    tasks.add("TEST");
    task.add("enum", tasks);
    writeProps.add(TASK, task);
    writeSchema.add(PROPERTIES, writeProps);

    // getDeviceState() emits a flat object, not a nested deviceState envelope.
    JsonObject root = new JsonObject();
    root.addProperty("$schema", "https://json-schema.org/draft/2020-12/schema");
    root.addProperty("title", title);
    root.addProperty("description", description);
    root.addProperty("type", OBJECT);
    JsonObject stateProperties = new JsonObject();
    stateProperties.add(DISPLAY, property(STRING, "Last text written to the display"));
    stateProperties.add(BLINK, blink.deepCopy());
    stateProperties.add(ENABLED, property("boolean", "Whether the LED driver is enabled"));
    stateProperties.add(BRIGHTNESS, brightness.deepCopy());
    root.add(PROPERTIES, stateProperties);
    root.addProperty("additionalProperties", false);

    // These describe updateDeviceConfiguration(), not getDeviceState().
    root.add("x-deviceStatic", configSchema);
    root.add("x-deviceWrite", writeSchema);
    return gson.toJson(root);
  }

  private JsonObject property(String type, String description) {
    JsonObject obj = new JsonObject();
    obj.addProperty("type", type);
    obj.addProperty("description", description);
    return obj;
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
