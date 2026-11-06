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

package io.mapsmessaging.devices.i2c.devices.storage.at24c;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.devices.storage.at24c.data.AT24CnnCommand;
import io.mapsmessaging.devices.i2c.devices.storage.at24c.data.AT24CnnResponse;
import io.mapsmessaging.devices.i2c.devices.storage.at24c.data.Details;
import io.mapsmessaging.devices.i2c.devices.storage.at24c.values.ActionType;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;

import java.io.IOException;

public class AT24CnnController extends I2CDeviceController {

  private final AT24CnnDevice sensor;

  // Used during ServiceLoading
  public AT24CnnController() {
    sensor = null;
  }

  @Override
  public String getName() {
    return "AT24C32/64";
  }

  @Override
  public String getDescription() {
    return "AT24C32/64/128/256/512 eeprom";
  }

  protected AT24CnnController(AddressableDevice device) throws IOException {
    super(device);
    sensor = new AT24CnnDevice(device);
  }

  public AT24CnnController(AddressableDevice device, int capacityBytes) throws IOException {
    super(device);
    sensor = new AT24CnnDevice(device, capacityBytes);
  }

  public I2CDevice getDevice() {
    return sensor;
  }

  public DeviceType getType() {
    return getDevice().getType();
  }

  @Override
  public boolean detect(AddressableDevice i2cDevice) {
    return sensor != null && sensor.isConnected();
  }

  public I2CDeviceController mount(AddressableDevice device) throws IOException {
    return new AT24CnnController(device);
  }

  public I2CDeviceController mount(AddressableDevice device, int capacityBytes) throws IOException {
    return new AT24CnnController(device, capacityBytes);
  }

  @Override
  public byte[] getDeviceConfiguration() throws IOException {
    return getDeviceState();
  }

  @Override
  public byte[] getDeviceState() throws IOException {
    if (sensor != null) {
      Details details = new Details(sensor.getName(), sensor.getMemorySize());
      ObjectMapper objectMapper = new ObjectMapper();
      return objectMapper.writeValueAsString(details).getBytes();
    }
    return "{}".getBytes();
  }

  @Override
  public byte[] updateDeviceConfiguration(byte[] val) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    JavaType type = objectMapper.getTypeFactory().constructType(AT24CnnCommand.class);
    AT24CnnCommand command = objectMapper.readValue(new String(val), type);
    AT24CnnResponse response = null;
    if (sensor != null) {
      if (command.getAction() == ActionType.READ) {
        byte[] data = sensor.readBlock(command.getAddress(), command.getLength());
        response = new AT24CnnResponse("Success", data);
      } else if (command.getAction() == ActionType.WRITE) {
        sensor.writeBlock(command.getAddress(), command.getData());
        response = new AT24CnnResponse("Success", new byte[0]);
      }
    } else {
      response = new AT24CnnResponse("Error", new byte[0]);
    }
    ObjectMapper objectMapper2 = new ObjectMapper();
    return objectMapper2.writeValueAsString(response).getBytes();
  }

  public SchemaConfig getSchema() {
    JsonObject state = new JsonObject();
    state.addProperty("type", "object");
    JsonObject stateProperties = new JsonObject();
    JsonObject name = new JsonObject();
    name.addProperty("type", "string");
    name.addProperty("description", "Configured AT24C EEPROM model");
    stateProperties.add("name", name);
    JsonObject size = new JsonObject();
    size.addProperty("type", "integer");
    size.addProperty("minimum", 4096);
    size.addProperty("maximum", 65536);
    size.addProperty("description", "Configured EEPROM capacity in bytes, not discovered by destructive probing");
    stateProperties.add("size", size);
    JsonObject typeId = new JsonObject();
    typeId.addProperty("type", "string");
    typeId.addProperty("description", "Optional Jackson @class discriminator");
    stateProperties.add("@class", typeId);
    state.add("properties", stateProperties);
    state.addProperty("additionalProperties", false);

    JsonObject command = new JsonObject();
    command.addProperty("type", "object");
    JsonObject commandProperties = new JsonObject();
    JsonObject action = new JsonObject();
    action.addProperty("type", "string");
    JsonArray actions = new JsonArray();
    actions.add("READ");
    actions.add("WRITE");
    action.add("enum", actions);
    commandProperties.add("action", action);
    JsonObject address = new JsonObject();
    address.addProperty("type", "integer");
    address.addProperty("minimum", 0);
    commandProperties.add("address", address);
    JsonObject length = new JsonObject();
    length.addProperty("type", "integer");
    length.addProperty("minimum", 0);
    length.addProperty("description", "Number of bytes to read");
    commandProperties.add("length", length);
    JsonObject data = new JsonObject();
    JsonArray formats = new JsonArray();
    JsonObject base64 = new JsonObject();
    base64.addProperty("type", "string");
    base64.addProperty("contentEncoding", "base64");
    formats.add(base64);
    JsonObject bytes = new JsonObject();
    bytes.addProperty("type", "array");
    JsonObject element = new JsonObject();
    element.addProperty("type", "integer");
    element.addProperty("minimum", 0);
    element.addProperty("maximum", 255);
    bytes.add("items", element);
    formats.add(bytes);
    data.add("anyOf", formats);
    data.addProperty("description", "Bytes to write; required for WRITE operations");
    commandProperties.add("data", data);
    commandProperties.add("@class", typeId.deepCopy());
    command.add("properties", commandProperties);
    state.add("x-deviceCommand", command);

    JsonObject response = new JsonObject();
    response.addProperty("type", "object");
    JsonObject responseProperties = new JsonObject();
    JsonObject status = new JsonObject();
    status.addProperty("type", "string");
    responseProperties.add("status", status);
    JsonObject responseData = new JsonObject();
    responseData.addProperty("type", "string");
    responseData.addProperty("contentEncoding", "base64");
    responseProperties.add("data", responseData);
    responseProperties.add("@class", typeId.deepCopy());
    response.add("properties", responseProperties);
    state.add("x-deviceResponse", response);

    JsonSchemaConfig config = new JsonSchemaConfig(gson.toJson(state));
    config.setComments("AT24C32/64/128/256/512 EEPROM state and read/write command schemas");
    config.setTitle(getName());
    config.setVersion(1);
    config.setResourceType("storage");
    config.setUniqueId(getSchemaId());
    config.setDescription("EEPROM state contains configured name and byte capacity; READ and WRITE command contracts are described separately");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    return new int[]{0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57};
  }
}