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

package io.mapsmessaging.devices.i2c.devices.sensors.tls2561;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.NamingConstants;
import io.mapsmessaging.devices.i2c.I2CDevice;
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

public class TLS2561Controller extends I2CDeviceController {

  private final TLS2561Sensor sensor;

  @Getter
  private final String name = "TLS2561";
  @Getter
  private final String description = "Light sensor, returns light and IR light levels and computed lux level";

  public TLS2561Controller() {
    sensor = null;
  }

  public TLS2561Controller(I2C device) throws IOException {
    super(device);
    sensor = new TLS2561Sensor(device);
  }

  public I2CDevice getDevice(){
    return sensor;
  }

  @Override
  public boolean detect(I2C i2cDevice) {
    return sensor != null && sensor.isConnected();
  }

  public I2CDeviceController mount(I2C device) throws IOException {
    return new TLS2561Controller(device);
  }

  public byte[] getStaticPayload() {
    JSONObject jsonObject = new JSONObject();
    if (sensor != null) {
      jsonObject.put("integration", sensor.getIntegrationTime().getTime());
      jsonObject.put("highGain", sensor.getHighGain() != 0);
    }
    return jsonObject.toString(2).getBytes();
  }

  public byte[] getUpdatePayload() throws IOException {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("ch0", sensor.getFull());
    jsonObject.put("ch1", sensor.getIr());
    jsonObject.put("lux", sensor.calculateLux());
    return jsonObject.toString(2).getBytes();
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig(buildSchema());
    config.setComments("i2c device TLS2561 light sensor, returns light and IR light levels and computed lux level");
    config.setSource("I2C bus address : 0x39");
    config.setVersion("1.0");
    config.setResourceType("sensor");
    config.setInterfaceDescription("Returns JSON object containing light and IR light levels");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    int i2cAddr = 0x39;
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
        .addPropertySchema("highGain",
            BooleanSchema.builder()
                .description("High Gain enabled or disabled")
                .build());

    ObjectSchema.Builder updateSchema = ObjectSchema.builder()
        .addPropertySchema("ch0",
            NumberSchema.builder()
                .minimum(0)
                .maximum(65535)
                .description("Light and IR levels")
                .build()
        )
        .addPropertySchema("ch1",
            NumberSchema.builder()
                .minimum(0)
                .maximum(65535)
                .description("IR levels")
                .build()
        )
        .addPropertySchema("lux",
            NumberSchema.builder()
                .description("Computed LUX value")
                .build()
        );

    ObjectSchema.Builder schemaBuilder = ObjectSchema.builder();
    schemaBuilder
        .addPropertySchema(NamingConstants.SENSOR_DATA_SCHEMA, updateSchema.build())
        .addPropertySchema(NamingConstants.DEVICE_STATIC_DATA_SCHEMA, staticSchema.build())
        .description("Digital Luminosity/Lux/Light Sensor Breakout")
        .title("TLS2561");

    ObjectSchema schema = schemaBuilder.build();
    return schema.toString();
  }
}
