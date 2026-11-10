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

package io.mapsmessaging.devices.onewire.devices.ds18b20;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.onewire.OneWireDeviceController;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class DS18B20Controller extends OneWireDeviceController {

  private final DS18B20Device sensor;

  public DS18B20Controller() {
    sensor = null;
  }

  @Override
  public String getName() {
    return "DS18B20";
  }

  @Override
  public String getDescription() {
    return "Temperature sensor";
  }

  public DS18B20Controller(File path) {
    sensor = new DS18B20Device(path);
  }

  public String getId() {
    return "28-";
  }

  @Override
  public OneWireDeviceController mount(File path) {
    return new DS18B20Controller(path);
  }

  public DeviceType getType() {
    return DeviceType.SENSOR;
  }

  public SchemaConfig getSchema() {
    JsonObject schema = new JsonObject();
    schema.addProperty("type", "object");
    JsonObject properties = new JsonObject();
    JsonObject temperature = new JsonObject();
    temperature.addProperty("type", "number");
    temperature.addProperty("minimum", -55);
    temperature.addProperty("maximum", 125);
    temperature.addProperty("readOnly", true);
    temperature.addProperty("description", "Current DS18B20 temperature in degrees Celsius");
    properties.add("temperature", temperature);
    schema.add("properties", properties);
    schema.addProperty("additionalProperties", false);
    JsonSchemaConfig config = new JsonSchemaConfig(gson.toJson(schema));
    config.setComments("1-Wire temperature sensor");
    config.setTitle(getName());
    config.setUniqueId(getSchemaId());
    config.setVersion(1);
    config.setResourceType("sensor");
    config.setDescription("Emits the current DS18B20 temperature in degrees Celsius; min/max/delta are available through the underlying sensor readings");
    return config;
  }

  @Override
  public byte[] getDeviceConfiguration() throws IOException {
    return getConfigEventDescription();
  }

  @Override
  public byte[] getDeviceState() {
    JsonObject jsonObject = new JsonObject();
    if (sensor != null) {
      sensor.update();
      jsonObject.add("temperature", new JsonPrimitive(sensor.getCurrent()));
    }
    return gson.toJson(jsonObject).getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public byte[] updateDeviceConfiguration(byte[] val) throws IOException {
    return new byte[0]; // Nothing to do
  }
}
