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

package io.mapsmessaging.devices.i2c.devices.gpio.mcp23017;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import io.mapsmessaging.schemas.model.XRegistrySchemaVersion;

import java.io.IOException;

public class Mcp23017Controller extends I2CDeviceController {

  private final Mcp23017Device sensor;

  // Used during ServiceLoading
  public Mcp23017Controller() {
    sensor = null;
  }

  @Override
  public String getName() {
    return "MCP23017";
  }

  @Override
  public String getDescription() {
    return "MCP23017 16 pin GPIO extender";
  }

  protected Mcp23017Controller(AddressableDevice device) throws IOException {
    super(device);
    sensor = new Mcp23017Device(device);
  }

  public DeviceType getType() {
    return sensor.getType();
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

  @Override
  public byte[] getDeviceConfiguration() throws IOException {
    return emptyJson();
  }

  @Override
  public byte[] getDeviceState() throws IOException {
    return emptyJson();
  }

  public XRegistrySchemaVersion getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig(buildSchema(sensor));
    config.setDescription("i2c device MCP32017 16 Pin extender");
    config.setComments(getName());
    config.setUniqueId(getSchemaId());
    config.setVersion("1");
    config.setResourceType("gpio");
    config.setInterfaceDescription("gpio extender");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    int i2cAddr = 0x27;
    return new int[]{i2cAddr};
  }
}
