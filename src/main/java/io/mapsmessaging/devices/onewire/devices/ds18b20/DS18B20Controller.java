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

package io.mapsmessaging.devices.onewire.devices.ds18b20;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.onewire.OneWireDeviceController;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import lombok.Getter;
import org.json.JSONObject;

import java.io.File;

public class DS18B20Controller extends OneWireDeviceController {

  private final DS18B20Device sensor;

  @Getter
  private final String name = "DS18B20";
  @Getter
  private final String description = "Temperature sensor";

  public DS18B20Controller() {
    sensor = null;
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
    JsonSchemaConfig config = new JsonSchemaConfig(buildSchema(sensor));
    config.setComments("1-Wire temperature sensor");
    config.setTitle(getName());
    config.setUniqueId(getSchemaId());
    config.setVersion("1.0");
    config.setResourceType("sensor");
    config.setInterfaceDescription("Returns JSON object containing temperature, minimum and maximum, Model, Status and Version");
    return config;
  }

  @Override
  public byte[] getDeviceConfiguration() {
    return "{}".getBytes();
  }

  @Override
  public byte[] getDeviceState() {
    JSONObject jsonObject = new JSONObject();
    if (sensor != null) {
      sensor.update();
      jsonObject.put("temperature", sensor.getCurrent());
    }
    return jsonObject.toString(2).getBytes();
  }
}
