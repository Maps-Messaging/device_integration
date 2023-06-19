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

package io.mapsmessaging.devices.oneWire.devices.ds18b20;

import io.mapsmessaging.devices.oneWire.OneWireDeviceEntry;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import lombok.Getter;
import org.json.JSONObject;

import java.io.File;

public class DS18B20Controller implements OneWireDeviceEntry {

  private final DS18B20Device sensor;

  @Getter
  private final String name = "DS18B20";

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
  public OneWireDeviceEntry mount(File path) {
    return new DS18B20Controller(path);
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig();
    config.setComments("1-Wire temperature sensor");
    config.setSource("1-wire");
    config.setVersion("1.0");
    config.setResourceType("sensor");
    config.setInterfaceDescription("Returns JSON object containing temperature, minimum and maximum, Model, Status and Version");
    return config;
  }

  @Override
  public byte[] getStaticPayload() {
    return new byte[0];
  }

  @Override
  public byte[] getUpdatePayload() {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("temperature", sensor.getCurrent());
    jsonObject.put("minimum", sensor.getMin());
    jsonObject.put("maximum", sensor.getMax());
    return jsonObject.toString(2).getBytes();
  }
}
