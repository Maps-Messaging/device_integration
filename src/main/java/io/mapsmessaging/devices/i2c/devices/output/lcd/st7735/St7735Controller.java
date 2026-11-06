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

package io.mapsmessaging.devices.i2c.devices.output.lcd.st7735;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.I2CDeviceScheduler;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class St7735Controller extends I2CDeviceController {
  private static final String FIELD_TYPE = "type";
  private static final String FIELD_PROPERTIES = "properties";
  private static final String FIELD_DESCRIPTION = "description";
  private static final String FIELD_SCHEMA = "$schema";
  private static final String FIELD_TITLE = "title";
  private static final String FIELD_DEVICE_STATIC = "deviceStatic";
  private static final String FIELD_DEVICE_WRITE = "deviceWrite";

  private static final String TYPE_OBJECT = "object";
  private static final String TYPE_STRING = "string";
  private static final String TYPE_NUMBER = "number";

  private static final String FIELD_WIDTH = "width";
  private static final String FIELD_HEIGHT = "height";
  private static final String FIELD_COMMAND = "command";
  private static final String FIELD_X = "x";
  private static final String FIELD_Y = "y";
  private static final String FIELD_COLOR = "color";
  private static final String FIELD_DATA = "data";


  private static final String NAME = "ST7735";
  private static final String DESCRIPTION = "ST7735 lcd display";

  private final St7735Device display;

  // Used during ServiceLoading
  public St7735Controller() {
    display = null;
  }

  protected St7735Controller(AddressableDevice device) throws IOException {
    super(device);
    synchronized (I2CDeviceScheduler.getI2cBusLock()) {
      display = new St7735Device(device);
    }
  }

  public I2CDevice getDevice() {
    return display;
  }

  public DeviceType getType() {
    return getDevice().getType();
  }


  @Override
  public boolean detect(AddressableDevice i2cDevice) {
    return display != null && display.isConnected();
  }

  public I2CDeviceController mount(AddressableDevice device) throws IOException {
    return new St7735Controller(device);
  }

  @Override
  public byte[] updateDeviceConfiguration(byte[] val) throws IOException {
    if (display == null) {
      throw new IOException("ST7735 display has not been mounted");
    }
    JsonObject command;
    try {
      command = JsonParser.parseString(new String(val, StandardCharsets.UTF_8)).getAsJsonObject();
    } catch (RuntimeException exception) {
      throw new IOException("Invalid ST7735 command JSON", exception);
    }
    if (command.has(FIELD_DEVICE_WRITE) && command.get(FIELD_DEVICE_WRITE).isJsonObject()) {
      command = command.getAsJsonObject(FIELD_DEVICE_WRITE);
    }
    if (!command.has(FIELD_COMMAND)) {
      throw new IOException("ST7735 command is required");
    }
    String action = command.get(FIELD_COMMAND).getAsString();
    int x = command.has(FIELD_X) ? command.get(FIELD_X).getAsInt() : 0;
    int y = command.has(FIELD_Y) ? command.get(FIELD_Y).getAsInt() : 0;
    int color = parseColor(command);
    try {
      synchronized (I2CDeviceScheduler.getI2cBusLock()) {
        switch (action) {
          case "CLEAR" -> display.lcdFillScreen(color);
          case "SET_PIXEL" -> display.lcdFillRectangle(x, y, 1, 1, color);
          case "DRAW_IMAGE" -> {
            if (!command.has("width") || !command.has("height") || !command.has(FIELD_DATA)) {
              throw new IOException("ST7735 DRAW_IMAGE requires width, height and Base64 data");
            }
            int width = command.get("width").getAsInt();
            int height = command.get("height").getAsInt();
            if (x < 0 || y < 0 || width <= 0 || height <= 0 || x + (long) width > 160 || y + (long) height > 80) {
              throw new IOException("ST7735 image coordinates exceed display bounds");
            }
            byte[] pixels = Base64.getDecoder().decode(command.get(FIELD_DATA).getAsString());
            if (pixels.length != (long) width * height * 2) {
              throw new IOException("ST7735 image must contain two RGB565 bytes per pixel");
            }
            display.lcdDrawImage(x, y, width, height, pixels);
          }
          default -> throw new IOException("Unsupported ST7735 command: " + action);
        }
      }
    } catch (IllegalArgumentException exception) {
      throw new IOException("Invalid ST7735 display parameters", exception);
    }
    JsonObject response = new JsonObject();
    response.addProperty("status", "success");
    response.addProperty(FIELD_COMMAND, action);
    return gson.toJson(response).getBytes(StandardCharsets.UTF_8);
  }

  private static int parseColor(JsonObject command) throws IOException {
    if (!command.has(FIELD_COLOR)) {
      return St7735Device.ST7735_BLACK;
    }
    String value = command.get(FIELD_COLOR).getAsString();
    return switch (value.toUpperCase(java.util.Locale.ROOT)) {
      case "BLACK" -> St7735Device.ST7735_BLACK;
      case "WHITE" -> St7735Device.ST7735_WHITE;
      case "RED" -> St7735Device.ST7735_RED;
      case "GREEN" -> St7735Device.ST7735_GREEN;
      case "BLUE" -> St7735Device.ST7735_BLUE;
      default -> {
        if (!value.matches("#[0-9a-fA-F]{6}")) {
          throw new IOException("ST7735 color must be #RRGGBB or a supported color name");
        }
        int rgb = Integer.parseInt(value.substring(1), 16);
        yield ((rgb >> 19) & 0x1f) << 11 | ((rgb >> 10) & 0x3f) << 5 | ((rgb >> 3) & 0x1f);
      }
    };
  }

  @Override
  public byte[] getDeviceConfiguration() throws IOException {
    return emptyJson();
  }

  @Override
  public byte[] getDeviceState() throws IOException {
    return emptyJson();
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String getDescription() {
    return DESCRIPTION;
  }

  @Override
  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig(buildSchema());
    config.setComments(DESCRIPTION);
    config.setTitle(getName());
    config.setVersion(1);
    config.setUniqueId(getSchemaId());
    config.setResourceType("display");
    config.setInterfaceDescription("display");
    return config;
  }

  private String buildSchema() {
    JsonObject writeSchema = new JsonObject();
    writeSchema.addProperty(FIELD_TYPE, TYPE_OBJECT);
    JsonObject writeProps = new JsonObject();
    JsonObject action = property(TYPE_STRING, "Implemented display command");
    JsonArray actions = new JsonArray();
    actions.add("CLEAR");
    actions.add("SET_PIXEL");
    actions.add("DRAW_IMAGE");
    action.add("enum", actions);
    writeProps.add(FIELD_COMMAND, action);

    JsonObject x = property("integer", "X coordinate, 0 through 159");
    x.addProperty("minimum", 0);
    x.addProperty("maximum", 159);
    writeProps.add(FIELD_X, x);
    JsonObject y = property("integer", "Y coordinate, 0 through 79");
    y.addProperty("minimum", 0);
    y.addProperty("maximum", 79);
    writeProps.add(FIELD_Y, y);
    writeProps.add(FIELD_COLOR, property(TYPE_STRING, "RGB hex #RRGGBB or BLACK, WHITE, RED, GREEN, BLUE"));

    JsonObject data = property(TYPE_STRING, "RGB565 pixel data in Base64, two bytes per pixel");
    data.addProperty("contentEncoding", "base64");
    writeProps.add(FIELD_DATA, data);
    JsonObject width = property("integer", "DRAW_IMAGE width");
    width.addProperty("minimum", 1);
    width.addProperty("maximum", 160);
    writeProps.add(FIELD_WIDTH, width);
    JsonObject height = property("integer", "DRAW_IMAGE height");
    height.addProperty("minimum", 1);
    height.addProperty("maximum", 80);
    writeProps.add(FIELD_HEIGHT, height);
    writeSchema.add(FIELD_PROPERTIES, writeProps);
    JsonArray required = new JsonArray();
    required.add(FIELD_COMMAND);
    writeSchema.add("required", required);

    // getDeviceState() and getDeviceConfiguration() both currently return {}.
    JsonObject emptyState = new JsonObject();
    emptyState.addProperty(FIELD_TYPE, TYPE_OBJECT);
    emptyState.add(FIELD_PROPERTIES, new JsonObject());
    emptyState.addProperty("additionalProperties", false);

    JsonObject response = new JsonObject();
    response.addProperty(FIELD_TYPE, TYPE_OBJECT);
    JsonObject responseProperties = new JsonObject();
    responseProperties.add("status", property(TYPE_STRING, "success"));
    responseProperties.add(FIELD_COMMAND, action.deepCopy());
    response.add(FIELD_PROPERTIES, responseProperties);

    JsonObject root = new JsonObject();
    root.addProperty(FIELD_SCHEMA, "https://json-schema.org/draft/2020-12/schema");
    root.addProperty(FIELD_TITLE, NAME);
    root.addProperty(FIELD_DESCRIPTION, "ST7735 state is currently empty; supported display operations have separate contracts");
    root.addProperty(FIELD_TYPE, TYPE_OBJECT);
    root.add(FIELD_PROPERTIES, emptyState.getAsJsonObject(FIELD_PROPERTIES));
    root.addProperty("additionalProperties", false);
    root.add("x-deviceWrite", writeSchema);
    root.add("x-deviceResponse", response);
    return gson.toJson(root);
  }

  private JsonObject property(String type, String description) {
    JsonObject obj = new JsonObject();
    obj.addProperty(FIELD_TYPE, type);
    obj.addProperty(FIELD_DESCRIPTION, description);
    return obj;
  }
  @Override
  public int[] getAddressRange() {
    return new int[]{0x18};
  }
}
