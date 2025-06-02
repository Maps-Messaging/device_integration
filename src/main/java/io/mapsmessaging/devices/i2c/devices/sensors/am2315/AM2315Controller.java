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

package io.mapsmessaging.devices.i2c.devices.sensors.am2315;

import com.google.gson.JsonObject;
import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import lombok.Getter;
import org.everit.json.schema.NumberSchema;
import org.everit.json.schema.ObjectSchema;

import java.io.IOException;

public class AM2315Controller extends I2CDeviceController {

  private final AM2315Sensor sensor;

  @Getter
  private final String name = "AM2315";
  @Getter
  private final String description = "AM2315 encased Temperature and Humidity Sensor";

  // Used during ServiceLoading
  public AM2315Controller() {
    sensor = null;
  }

  protected AM2315Controller(AddressableDevice device) throws IOException {
    super(device);
    sensor = new AM2315Sensor(device);
  }

  public I2CDevice getDevice() {
    return sensor;
  }

  @Override
  public boolean detect(AddressableDevice i2cDevice) {
    return sensor != null && sensor.isConnected();
  }

  public I2CDeviceController mount(AddressableDevice device) throws IOException {
    return new AM2315Controller(device);
  }

  public DeviceType getType() {
    return getDevice().getType();
  }

  @Override
  public byte[] getDeviceConfiguration() throws IOException {
    JsonObject jsonObject = new JsonObject();
    if (sensor != null) {
      jsonObject.addProperty("model", sensor.getModel());
      jsonObject.addProperty("status", sensor.getStatus());
      jsonObject.addProperty("version", sensor.getVersion());
      jsonObject.addProperty("id", sensor.getId());
    }
    return convert(jsonObject);
  }

  @Override
  public byte[] getDeviceState() throws IOException {
    JsonObject jsonObject = new JsonObject();
    if (sensor != null) {
      jsonObject.addProperty("temperature", sensor.getTemperature());
      jsonObject.addProperty("humidity", sensor.getHumidity());
    }
    return convert(jsonObject);
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig(buildSchema());
    config.setComments("i2c device AM2315 encased Temperature and Humidity Sensor https://www.adafruit.com/product/1293");
    config.setTitle(getName());
    config.setVersion("1.0");
    config.setUniqueId(getSchemaId());
    config.setResourceType("sensor");
    config.setInterfaceDescription("temperature, humidity");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    int i2cAddr = 0x5C;
    return new int[]{i2cAddr};
  }


  private String buildSchema() {
    ObjectSchema staticSchema = ObjectSchema.builder()
        .addPropertySchema("model",
            NumberSchema.builder()
                .description("Model number of sensor")
                .build())
        .addPropertySchema("id",
            NumberSchema.builder()
                .description("Unique ID of sensor")
                .build())
        .addPropertySchema("status",
            NumberSchema.builder()
                .description("Current status bitmask")
                .build())
        .addPropertySchema("version",
            NumberSchema.builder()
                .description("Chip version")
                .build())
        .build();

    return buildSchema(sensor, staticSchema);
  }


}
