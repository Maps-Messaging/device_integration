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

package io.mapsmessaging.devices.i2c.devices.storage.at24c;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import lombok.Getter;

import java.io.IOException;

public class AT24CnnController extends I2CDeviceController {

  private final AT24CnnDevice sensor;

  @Getter
  private final String name = "AT24C32/64";
  @Getter
  private final String description = "AT24C32/64/128/256/512 eeprom";

  // Used during ServiceLoading
  public AT24CnnController() {
    sensor = null;
  }

  protected AT24CnnController(AddressableDevice device) throws IOException {
    super(device);
    sensor = new AT24CnnDevice(device);
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

  public byte[] getDeviceConfiguration() throws IOException {
    return getDeviceState();
  }

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
    JsonSchemaConfig config = new JsonSchemaConfig();
    config.setComments("i2c device AT24C32/64 eeprom");
    config.setTitle(getName());
    config.setVersion("1.0");
    config.setResourceType("storage");
    config.setUniqueId(getSchemaId());
    config.setInterfaceDescription("Serial EEPROM");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    return new int[]{0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57};
  }
}