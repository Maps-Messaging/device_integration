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

package io.mapsmessaging.devices.i2c.devices.sensors.am2320;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.i2c.I2CDeviceEntry;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import lombok.Getter;
import org.everit.json.schema.*;
import org.json.JSONObject;

import java.io.IOException;

public class AM2320Controller implements I2CDeviceEntry {

  private final int i2cAddr = 0x5C;
  private final AM2320Sensor sensor;
  @Getter
  private final String name = "AM2320";

  public AM2320Controller() {
    sensor = null;
  }

  protected AM2320Controller(I2C device) throws IOException {
    sensor = new AM2320Sensor(device);
  }


  public I2CDeviceEntry mount(I2C device) throws IOException {
    return new AM2320Controller(device);
  }

  public byte[] getStaticPayload() {
    return "{}".getBytes();
  }


  public byte[] getUpdatePayload() {
    sensor.scanForChange();
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("humidity", sensor.getHumidity());
    jsonObject.put("temperature", sensor.getTemperature());
    return jsonObject.toString(2).getBytes();
  }

  @Override
  public boolean detect() {
    return sensor != null && sensor.isConnected();
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig(buildSchema());
    config.setComments("i2c device AM2320 Pressure and Temperature Sensor https://learn.adafruit.com/adafruit-am2320-temperature-humidity-i2c-sensor");
    config.setSource("I2C bus address : 0x5C");
    config.setVersion("1.0");
    config.setResourceType("sensor");
    config.setInterfaceDescription("Returns JSON object containing Temperature and Pressure");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    return new int[]{i2cAddr};
  }

  private String buildSchema() {
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
        .addPropertySchema("updateSchema", updateSchema.build())
        .description("Humidity and Temperature Module")
        .title("AM2320");

    return schemaToString(schemaBuilder.build());
  }
}
