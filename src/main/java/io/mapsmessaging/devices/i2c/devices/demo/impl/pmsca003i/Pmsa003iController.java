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

package io.mapsmessaging.devices.i2c.devices.demo.impl.pmsca003i;

import com.google.gson.JsonObject;
import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.devices.demo.I2cDemoController;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Pmsa003iController extends I2cDemoController {

  private static final int i2cAddr = 0x12;
  private final Pmsa003iSensor sensor;

  public Pmsa003iController() {
    super(null);
    sensor = null;
  }

  @Override
  public String getName() {
    return "Demo PMSA003I";
  }

  @Override
  public String getDescription() {
    return "Demo Air Quality sensor";
  }

  public Pmsa003iController(AddressableDevice device) {
    super(device);
    sensor = new Pmsa003iSensor(device);
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

  public I2CDeviceController mount(AddressableDevice device) {
    return new Pmsa003iController(device);
  }

  @Override
  public byte[] getDeviceConfiguration() throws IOException {
    JsonObject jsonObject = new JsonObject();
    if (sensor != null) {
      jsonObject.addProperty("version", "1.0 Demo");
    }
    return gson.toJson(jsonObject).getBytes(StandardCharsets.UTF_8);
  }

  public JsonObject pack() {
    return new JsonObject();
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig();
    config.setComments("Air Quality Breakout");
    config.setTitle(getName());
    config.setVersion(1);
    config.setResourceType("sensor");
    config.setUniqueId(getSchemaId());
    config.setDescription("Air Quality Breakout");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    return new int[]{i2cAddr};
  }
}