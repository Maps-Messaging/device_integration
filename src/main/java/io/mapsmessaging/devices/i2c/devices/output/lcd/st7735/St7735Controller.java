/*
 *
 *  Copyright [ 2020 - 2024 ] [Matthew Buckton]
 *  Copyright [ 2024 - 2025.  ] [Maps Messaging B.V.]
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package io.mapsmessaging.devices.i2c.devices.output.lcd.st7735;

import com.google.gson.JsonObject;
import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.I2CDeviceScheduler;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.devices.util.UuidGenerator;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;

import java.io.IOException;

public class St7735Controller extends I2CDeviceController {

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
    return new byte[0];
  }

  @Override
  public byte[] getDeviceConfiguration() throws IOException {
    JsonObject jsonObject = new JsonObject();
    if (display != null) {
      //
    }
    return convert(jsonObject);
  }

  @Override
  public byte[] getDeviceState() throws IOException {
    JsonObject jsonObject = new JsonObject();
    if (display != null) {
      //
    }
    return convert(jsonObject);
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String getDescription() {
    return DESCRIPTION;
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig(buildSchema());
    config.setComments(DESCRIPTION);
    config.setSource(getName());
    config.setVersion("1.0");
    config.setUniqueId(UuidGenerator.getInstance().generateUuid(getName()));
    config.setResourceType("display");
    config.setInterfaceDescription("display");
    return config;
  }

  private String buildSchema() {
    JsonObject configSchema = new JsonObject();
    configSchema.addProperty("type", "object");

    JsonObject configProps = new JsonObject();
    configProps.add("width", property("number", "Display width in pixels"));
    configProps.add("height", property("number", "Display height in pixels"));
    configProps.add("colorMode", property("string", "Color mode (e.g. RGB565)"));
    configSchema.add("properties", configProps);

    JsonObject writeSchema = new JsonObject();
    writeSchema.addProperty("type", "object");

    JsonObject writeProps = new JsonObject();
    writeProps.add("command", property("string", "Display command: DRAW_IMAGE, CLEAR, SET_PIXEL, etc."));
    writeProps.add("x", property("number", "X coordinate"));
    writeProps.add("y", property("number", "Y coordinate"));
    writeProps.add("color", property("string", "Color in hex format (#RRGGBB) or named value"));
    writeProps.add("data", property("string", "Optional image or buffer data as Base64 or string"));
    writeSchema.add("properties", writeProps);

    JsonObject root = new JsonObject();
    root.addProperty("$schema", "https://json-schema.org/draft/2020-12/schema");
    root.addProperty("title", "ST7735");
    root.addProperty("description", "ST7735 RGB LCD Display");
    root.addProperty("type", "object");

    JsonObject props = new JsonObject();
    props.add("deviceStatic", configSchema);
    props.add("deviceWrite", writeSchema);
    root.add("properties", props);

    return gson.toJson(root);
  }

  private JsonObject property(String type, String description) {
    JsonObject obj = new JsonObject();
    obj.addProperty("type", type);
    obj.addProperty("description", description);
    return obj;
  }


  @Override
  public int[] getAddressRange() {
    return new int[]{0x18};
  }
}
