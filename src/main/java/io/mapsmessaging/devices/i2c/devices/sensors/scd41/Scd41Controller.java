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

package io.mapsmessaging.devices.i2c.devices.sensors.scd41;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import lombok.Getter;

import java.io.IOException;

public class Scd41Controller extends I2CDeviceController {

  private final Scd41Sensor sensor;

  @Getter
  private final String name = "SCD-41";
  @Getter
  private final String description = "CO2 Sensor";


  public Scd41Controller() {
    sensor = null;
  }

  public Scd41Controller(AddressableDevice device) throws IOException {
    super(device);
    sensor = new Scd41Sensor(device);
  }

  public I2CDevice getDevice() {
    return sensor;
  }

  public DeviceType getType() {
    return getDevice().getType();
  }

  @Override
  public boolean canDetect() {
    return true;
  }

  @Override
  public boolean detect(AddressableDevice i2cDevice) {
    return Scd41Sensor.detect(i2cDevice);
  }

  public I2CDeviceController mount(AddressableDevice device) throws IOException {
    return new Scd41Controller(device);
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig(buildSchema());
    config.setComments("i2c device SCD-41 CO2 sensor: 400-2000 ppm");
    config.setTitle(getName());
    config.setVersion("1.0");
    config.setResourceType("sensor");
    config.setUniqueId(getSchemaId());
    config.setInterfaceDescription("Returns JSON object containing CO2 levels");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    int i2cAddr = 0x62;
    return new int[]{i2cAddr};
  }

  private String buildSchema() {
    return "{\n" +
        "  \"$schema\": \"http://json-schema.org/draft-07/schema#\",\n" +
        "  \"title\": \"SensorReadings\",\n" +
        "  \"type\": \"object\",\n" +
        "  \"properties\": {\n" +
        "    \"CO₂\": {\n" +
        "      \"type\": \"integer\",\n" +
        "      \"description\": \"The CO₂ reading in ppm\",\n" +
        "      \"minimum\": 400,\n" +
        "      \"maximum\": 40000\n" +
        "    },\n" +
        "    \"humidity\": {\n" +
        "      \"type\": \"number\",\n" +
        "      \"description\": \"The humidity reading as a percentage\",\n" +
        "      \"minimum\": 0,\n" +
        "      \"maximum\": 100,\n" +
        "      \"multipleOf\": 0.01\n" +
        "    },\n" +
        "    \"temperature\": {\n" +
        "      \"type\": \"number\",\n" +
        "      \"description\": \"The temperature reading in degrees Celsius\",\n" +
        "      \"minimum\": -10,\n" +
        "      \"maximum\": 60,\n" +
        "      \"multipleOf\": 0.01\n" +
        "    },\n" +
        "    \"airQuality\": {\n" +
        "      \"type\": \"string\",\n" +
        "      \"description\": \"The air quality category\"\n" +
        "    }\n" +
        "  },\n" +
        "  \"required\": [\"CO₂\", \"humidity\", \"temperature\", \"airQuality\"],\n" +
        "  \"additionalProperties\": false\n" +
        "}\n";
  }
}
