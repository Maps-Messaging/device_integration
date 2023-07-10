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

package io.mapsmessaging.devices.i2c.devices.sensors.am2315;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.NamingConstants;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import lombok.Getter;
import org.everit.json.schema.BooleanSchema;
import org.everit.json.schema.NumberSchema;
import org.everit.json.schema.ObjectSchema;
import org.everit.json.schema.StringSchema;
import org.json.JSONObject;

import java.io.IOException;

public class AM2315Controller extends I2CDeviceController {

  private final AM2315Sensor sensor;

  @Getter
  private final String name = "AM2315";

  // Used during ServiceLoading
  public AM2315Controller() {
    sensor = null;
  }

  protected AM2315Controller(I2C device) throws IOException {
    super(device);
    sensor = new AM2315Sensor(device);
  }

  @Override
  public boolean detect() {
    return sensor != null && sensor.isConnected();
  }

  public I2CDeviceController mount(I2C device) throws IOException {
    return new AM2315Controller(device);
  }

  public byte[] getStaticPayload() {
    JSONObject jsonObject = new JSONObject();
    if (sensor != null) {
      jsonObject.put("model", sensor.getModel());
      jsonObject.put("status", sensor.getStatus());
      jsonObject.put("version", sensor.getVersion());
      jsonObject.put("id", sensor.getId());
    }
    return jsonObject.toString(2).getBytes();
  }

  public byte[] getUpdatePayload() {
    JSONObject jsonObject = new JSONObject();
    if (sensor != null) {
      jsonObject.put("temperature", sensor.getTemperature());
      jsonObject.put("humidity", sensor.getHumidity());
    }
    return jsonObject.toString(2).getBytes();
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig(buildSchema());
    config.setComments("i2c device AM2315 encased Temperature and Humidity Sensor https://www.adafruit.com/product/1293");
    config.setSource("I2C bus address : 0x5C");
    config.setVersion("1.0");
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
    ObjectSchema.Builder staticSchema = ObjectSchema.builder()
        .addPropertySchema("model",
            StringSchema.builder()
                .description("Model number of sensor")
                .build()
        )
        .addPropertySchema("id",
            StringSchema.builder()
                .description("Unique ID of sensor")
                .build()
        )
        .addPropertySchema("status",
            BooleanSchema.builder()
                .description("Current Status")
                .build())
        .addPropertySchema("version",
            BooleanSchema.builder()
                .description("Chip Version")
                .build());

    ObjectSchema.Builder updateSchema = ObjectSchema.builder()
        .addPropertySchema("temperature",
            NumberSchema.builder()
                .minimum(-40.0)
                .maximum(80.0)
                .description("Temperature")
                .build()
        )
        .addPropertySchema("humidity",
            NumberSchema.builder()
                .minimum(0.0)
                .maximum(100.0)
                .description("Humidity")
                .build()
        );

    ObjectSchema.Builder schemaBuilder = ObjectSchema.builder();
    schemaBuilder
        .addPropertySchema(NamingConstants.SENSOR_DATA_SCHEMA, updateSchema.build())
        .addPropertySchema(NamingConstants.DEVICE_STATIC_DATA_SCHEMA, staticSchema.build())
        .description("Humidity and Temperature Module")
        .title("AM2315");

    return schemaToString(schemaBuilder.build());
  }
}
