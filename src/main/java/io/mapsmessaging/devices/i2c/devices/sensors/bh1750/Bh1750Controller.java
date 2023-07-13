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

package io.mapsmessaging.devices.i2c.devices.sensors.bh1750;

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

public class Bh1750Controller extends I2CDeviceController {

  private final Bh1750Sensor sensor;

  @Getter
  private final String name = "BH1750";
  @Getter
  private final String description = "Light sensor, returns ambient light levels";

  public Bh1750Controller() {
    sensor = null;
  }

  public Bh1750Controller(I2C device) throws IOException {
    super(device);
    sensor = new Bh1750Sensor(device);
  }

  @Override
  public boolean detect(I2C i2cDevice) {
    return sensor != null && sensor.isConnected();
  }

  public I2CDeviceController mount(I2C device) throws IOException {
    return new Bh1750Controller(device);
  }

  public byte[] getStaticPayload() {
    JSONObject jsonObject = new JSONObject();
    if (sensor != null) {
      jsonObject.put("sensorMode", sensor.getSensorReading().name());
      jsonObject.put("resolution", sensor.getResolutionMode().name());
    }
    return jsonObject.toString(2).getBytes();
  }

  public byte[] getUpdatePayload() throws IOException {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("lux", sensor.getLux());
    return jsonObject.toString(2).getBytes();
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig(buildSchema());
    config.setComments("i2c device BH1750 ambient light sensor in lux");
    config.setSource("I2C bus address : 0x23");
    config.setVersion("1.0");
    config.setResourceType("sensor");
    config.setInterfaceDescription("Returns JSON object containing current lux level");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    int i2cAddr = 0x23;
    return new int[]{i2cAddr};
  }

  private String buildSchema() {
    ObjectSchema.Builder staticSchema = ObjectSchema.builder()
        .addPropertySchema("integration",
            StringSchema.builder()
                .pattern("^MS_\\d{1,3}$")
                .description("Integration time to compute the values, 14ms, 101ms and 402ms default 402")
                .build()
        )
        .addPropertySchema("sensorMode",
            BooleanSchema.builder()
                .description("High Gain enabled or disabled")
                .build());

    ObjectSchema.Builder updateSchema = ObjectSchema.builder()
        .addPropertySchema("lux",
            NumberSchema.builder()
                .description("Computed LUX value")
                .build()
        );

    ObjectSchema.Builder schemaBuilder = ObjectSchema.builder();
    schemaBuilder
        .addPropertySchema(NamingConstants.SENSOR_DATA_SCHEMA, updateSchema.build())
        .addPropertySchema(NamingConstants.DEVICE_STATIC_DATA_SCHEMA, staticSchema.build())
        .description("Ambient light sensor")
        .title("BH1750");

    ObjectSchema schema = schemaBuilder.build();
    return schema.toString();
  }
}
