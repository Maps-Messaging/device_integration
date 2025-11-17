/*
 *
 *  Copyright [ 2020 - 2024 ] Matthew Buckton
 *  Copyright [ 2024 - 2025 ] MapsMessaging B.V.
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


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.I2CDeviceScheduler;
import io.mapsmessaging.devices.i2c.devices.output.lcd.lcd1602.data.ActionType;
import io.mapsmessaging.devices.i2c.devices.output.lcd.lcd1602.data.Lcd1602Command;
import io.mapsmessaging.devices.i2c.devices.output.lcd.lcd1602.data.Lcd1602Response;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;

import java.io.IOException;

public class Lcd1602Controller extends I2CDeviceController {
  private static final String TYPE_OBJECT = "object";
  private static final String TYPE_STRING = "string";
  private static final String TYPE_NUMBER = "number";
  private static final String TYPE_ARRAY = "array";
  private static final String FIELD_ROWS = "rows";
  private static final String FIELD_COLUMNS = "columns";
  private static final String FIELD_ACTION = "action";
  private static final String FIELD_ADDRESS = "address";
  private static final String FIELD_DATA = "data";
  private static final String FIELD_PROPERTIES = "properties";
  private static final String FIELD_TYPE = "type";
  private static final String FIELD_DESCRIPTION = "description";
  private static final String FIELD_SCHEMA = "$schema";
  private static final String FIELD_TITLE = "title";
  private static final String FIELD_DEVICE_STATIC = "deviceStatic";
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
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    JavaType type = objectMapper.getTypeFactory().constructType(Lcd1602Command.class);
    Lcd1602Response response = null;
    Lcd1602Command command = null;

    try {
      command = objectMapper.readValue(new String(val), type);
    } catch (IOException e) {
      // todo
    }
    if (command != null) {
      if (display != null) {
        if (command.getAction() == ActionType.READ) {
          byte[] data = display.readBlock(command.getAddress(), command.getLength());
          response = new Lcd1602Response(SUCCESS, data);
        } else if (command.getAction() == ActionType.WRITE) {
          display.writeBlock(command.getAddress(), command.getData());
          response = new Lcd1602Response(SUCCESS, new byte[0]);
        } else if (command.getAction() == ActionType.CLEAR) {
          display.clearDisplay();
          response = new Lcd1602Response(SUCCESS, new byte[0]);
        }
      } else {
        response = new Lcd1602Response(ERROR, new byte[0]);
      }
    } else {
      display.clearDisplay();
      display.writeBlock(0, val);
      response = new Lcd1602Response(SUCCESS, new byte[0]);
    }
    ObjectMapper objectMapper2 = new ObjectMapper();
    return objectMapper2.writeValueAsString(response).getBytes();
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
    JsonObject configSchema = new JsonObject();
    JsonObject configProps = new JsonObject();
    configProps.add(FIELD_ROWS, property(TYPE_NUMBER, "Number of display rows (typically 2)"));
    configProps.add(FIELD_COLUMNS, property(TYPE_NUMBER, "Number of display columns (typically 16 or 20)"));
    configSchema.addProperty(FIELD_TYPE, TYPE_OBJECT);
    configSchema.add(FIELD_PROPERTIES, configProps);

    JsonObject writeSchema = new JsonObject();
    JsonObject writeProps = new JsonObject();
    writeProps.add(FIELD_ACTION, property(TYPE_STRING, "Display action: WRITE, CLEAR, READ"));
    writeProps.add(FIELD_ADDRESS, property(TYPE_NUMBER, "Starting address in the buffer (0-based)"));
    JsonObject array = new JsonObject();
    array.addProperty(FIELD_TYPE, TYPE_ARRAY);
    array.addProperty(FIELD_DESCRIPTION, "Array of bytes to display (ASCII values)");
    writeProps.add(FIELD_DATA, array);
    writeSchema.addProperty(FIELD_TYPE, TYPE_OBJECT);
    writeSchema.add(FIELD_PROPERTIES, writeProps);

    JsonObject root = new JsonObject();
    root.addProperty(FIELD_SCHEMA, "https://json-schema.org/draft/2020-12/schema");
    root.addProperty(FIELD_TITLE, NAME);
    root.addProperty(FIELD_DESCRIPTION, "LCD1602 Character Display");
    JsonObject props = new JsonObject();
    props.add(FIELD_DEVICE_STATIC, configSchema);
    props.add(FIELD_DEVICE_WRITE, writeSchema);
    root.add(FIELD_PROPERTIES, props);
    root.addProperty(FIELD_TYPE, TYPE_OBJECT);

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
