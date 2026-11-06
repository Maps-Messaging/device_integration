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

package io.mapsmessaging.devices.i2c.devices.output.lcd.lcd1602;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.I2CDeviceScheduler;
import io.mapsmessaging.devices.i2c.devices.output.lcd.lcd1602.data.ActionType;
import io.mapsmessaging.devices.i2c.devices.output.lcd.lcd1602.data.Lcd1602Response;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;

import java.io.IOException;

public class Lcd1602Controller extends I2CDeviceController {
  private static final String SCHEMA_INTEGER = "integer";
  private static final String SCHEMA_MINIMUM = "minimum";
  private static final String TYPE_OBJECT = "object";
  private static final String TYPE_STRING = "string";
  private static final String TYPE_ARRAY = "array";
  private static final String FIELD_ACTION = "action";
  private static final String FIELD_ADDRESS = "address";
  private static final String FIELD_DATA = "data";
  private static final String FIELD_PROPERTIES = "properties";
  private static final String FIELD_TYPE = "type";
  private static final String FIELD_DESCRIPTION = "description";
  private static final String FIELD_SCHEMA = "$schema";
  private static final String FIELD_TITLE = "title";
  private static final String FIELD_DEVICE_WRITE = "deviceWrite";

  private static final String SUCCESS = "success";
  private static final String ERROR = "error";
  private static final String NAME = "LCD1602";
  private static final String DESCRIPTION = "LCD1602 16*2 lcd display";

  private final Lcd1602Device display;

  // Used during ServiceLoading
  public Lcd1602Controller() {
    display = null;
  }

  protected Lcd1602Controller(AddressableDevice device) {
    super(device);
    synchronized (I2CDeviceScheduler.getI2cBusLock()) {
      display = new Lcd1602Device(device);
      display.clearDisplay();
      display.setRows(2);
      display.setColumns(16);
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
    return new Lcd1602Controller(device);
  }

  @Override
  public byte[] updateDeviceConfiguration(byte[] val) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    JsonNode payload;
    try {
      payload = mapper.readTree(val);
    } catch (IOException _) {
      payload = null; // Preserve the existing plain-text display interface.
    }
    if (payload != null && payload.isObject()) {
      if (payload.has(FIELD_DEVICE_WRITE) && payload.get(FIELD_DEVICE_WRITE).isObject()) {
        payload = payload.get(FIELD_DEVICE_WRITE);
      }
      if (display == null) {
        return mapper.writeValueAsBytes(new Lcd1602Response(ERROR, new byte[0]));
      }
      return executeCommand(mapper, payload);
    }
    if (display == null) {
      return mapper.writeValueAsBytes(new Lcd1602Response(ERROR, new byte[0]));
    }
    display.clearDisplay();
    display.writeBlock(0, val);
    return mapper.writeValueAsBytes(new Lcd1602Response(SUCCESS, new byte[0]));
  }

  private byte[] executeCommand(ObjectMapper mapper, JsonNode payload) throws IOException {
    JsonNode actionNode = payload.get(FIELD_ACTION);
    if (actionNode == null || !actionNode.isTextual()) {
      throw new IOException("LCD1602 command requires an action");
    }
    ActionType action;
    try {
      action = ActionType.valueOf(actionNode.asText());
    } catch (IllegalArgumentException exception) {
      throw new IOException("Unknown LCD1602 action: " + actionNode.asText(), exception);
    }
    int address = payload.path(FIELD_ADDRESS).asInt(0);
    Lcd1602Response response;
    switch (action) {
      case READ -> response = new Lcd1602Response(SUCCESS,
          display.readBlock(address, payload.path("length").asInt(0)));
      case WRITE -> {
        JsonNode data = payload.get(FIELD_DATA);
        if (data == null || data.isNull()) {
          throw new IOException("LCD1602 WRITE requires data");
        }
        display.writeBlock(address, mapper.treeToValue(data, byte[].class));
        response = new Lcd1602Response(SUCCESS, new byte[0]);
      }
      case CLEAR -> {
        display.clearDisplay();
        response = new Lcd1602Response(SUCCESS, new byte[0]);
      }
      default -> throw new IOException("Unsupported LCD1602 action");
    }
    return mapper.writeValueAsBytes(response);
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
    config.setUniqueId(getSchemaId());
    config.setVersion(1);
    config.setResourceType("display");
    config.setDescription("LCD1602 accepts actions such as WRITE, CLEAR, READ. Data is a byte array.");
    return config;
  }
  private String buildSchema() {
    JsonObject writeSchema = new JsonObject();
    writeSchema.addProperty(FIELD_TYPE, TYPE_OBJECT);
    JsonObject writeProps = new JsonObject();
    JsonObject action = property(TYPE_STRING, "LCD1602 action accepted by updateDeviceConfiguration");
    JsonArray actions = new JsonArray();
    actions.add("WRITE");
    actions.add("READ");
    actions.add("CLEAR");
    action.add("enum", actions);
    writeProps.add(FIELD_ACTION, action);
    JsonObject address = property(SCHEMA_INTEGER, "Starting offset in the 32-byte display buffer");
    address.addProperty(SCHEMA_MINIMUM, 0);
    address.addProperty("maximum", 32);
    writeProps.add(FIELD_ADDRESS, address);
    JsonObject length = property(SCHEMA_INTEGER, "Number of bytes to return for READ");
    length.addProperty(SCHEMA_MINIMUM, 0);
    writeProps.add("length", length);
    JsonObject data = property(TYPE_ARRAY, "Unsigned byte values to write for WRITE");
    JsonObject element = property(SCHEMA_INTEGER, "Character byte");
    element.addProperty(SCHEMA_MINIMUM, 0);
    element.addProperty("maximum", 255);
    data.add("items", element);
    writeProps.add(FIELD_DATA, data);
    writeSchema.add(FIELD_PROPERTIES, writeProps);
    JsonArray requiredAction = new JsonArray();
    requiredAction.add(FIELD_ACTION);
    writeSchema.add("required", requiredAction);

    // This controller currently reports {}, not the display buffer.
    JsonObject emptyState = new JsonObject();
    emptyState.addProperty(FIELD_TYPE, TYPE_OBJECT);
    emptyState.add(FIELD_PROPERTIES, new JsonObject());
    emptyState.addProperty("additionalProperties", false);

    JsonObject response = new JsonObject();
    response.addProperty(FIELD_TYPE, TYPE_OBJECT);
    JsonObject responseProps = new JsonObject();
    responseProps.add("status", property(TYPE_STRING, "success or error"));
    JsonObject responseData = property(TYPE_STRING, "READ result bytes serialized as Base64");
    responseData.addProperty("contentEncoding", "base64");
    responseProps.add(FIELD_DATA, responseData);
    responseProps.add("@class", property(TYPE_STRING, "Optional Jackson type discriminator"));
    response.add(FIELD_PROPERTIES, responseProps);

    JsonObject root = new JsonObject();
    root.addProperty(FIELD_SCHEMA, "https://json-schema.org/draft/2020-12/schema");
    root.addProperty(FIELD_TITLE, NAME);
    root.addProperty(FIELD_DESCRIPTION, "LCD1602 state is currently an empty object; command and response contracts are separate");
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
    return new int[]{0x3e};
  }
}
