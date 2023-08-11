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

package io.mapsmessaging.devices.i2c.devices.gpio.mcp23017;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import lombok.Getter;
import org.json.JSONObject;

import java.io.IOException;

public class Mcp23017Controller extends I2CDeviceController {

  private final Mcp23017Device sensor;

  @Getter
  private final String name = "MCP23017";
  @Getter
  private final String description = "MCP23017 16 pin GPIO extender";

  // Used during ServiceLoading
  public Mcp23017Controller() {
    sensor = null;
  }

  protected Mcp23017Controller(AddressableDevice device) throws IOException {
    super(device);
    sensor = new Mcp23017Device(device);
  }

  public I2CDevice getDevice() {
    return sensor;
  }

  @Override
  public boolean detect(AddressableDevice i2cDevice) {
    return sensor != null && sensor.isConnected();
  }

  public I2CDeviceController mount(AddressableDevice device) throws IOException {
    return new Mcp23017Controller(device);
  }

  public byte[] getDeviceConfiguration() throws IOException {
    JSONObject jsonObject = new JSONObject();
    if (sensor != null) {
    }
    return jsonObject.toString(2).getBytes();
  }

  public byte[] getDeviceState() throws IOException {
    JSONObject jsonObject = new JSONObject();
    if (sensor != null) {
    }
    return jsonObject.toString(2).getBytes();
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig();
    config.setComments("i2c device MCP32017 16 Pin extender");
    config.setSource("I2C bus address : 0x20");
    config.setVersion("1.0");
    config.setResourceType("gpio");
    config.setInterfaceDescription("gpio extender");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    int i2cAddr = 0x20;
    return new int[]{i2cAddr};
  }
}
