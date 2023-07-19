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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.NamingConstants;
import io.mapsmessaging.devices.deviceinterfaces.AbstractRegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.devices.sensors.bh1750.values.ResolutionMode;
import io.mapsmessaging.devices.i2c.devices.sensors.bh1750.values.SensorReadingMode;
import io.mapsmessaging.devices.sensorreadings.ComputationResult;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import lombok.Getter;
import org.everit.json.schema.EnumSchema;
import org.everit.json.schema.NumberSchema;
import org.everit.json.schema.ObjectSchema;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class Bh1750Controller extends I2CDeviceController {

  private static final String SENSOR_READING = "sensorReading";
  private static final String RESOLUTION_MODE = "resolutionMode";
  private static final String LUX = "lux";

  @Getter
  private final String name = "BH1750";

  @Getter
  private final String description = "16bit Serial Output Type Ambient Light Sensor ";

  private final Bh1750Sensor sensor;


  public Bh1750Controller() {
    sensor = null;
  }

  public Bh1750Controller(I2C device) throws IOException {
    super(device);
    sensor = new Bh1750Sensor(device);
  }

  public I2CDevice getDevice(){
    return sensor;
  }

  @Override
  public boolean detect(I2C i2cDevice) {
    return sensor != null && sensor.isConnected();
  }

  public I2CDeviceController mount(I2C device) throws IOException {
    return new Bh1750Controller(device);
  }

  public byte[] getDeviceConfiguration() throws IOException {
    JSONObject jsonObject = new JSONObject();
    if (sensor != null) {
      ObjectMapper objectMapper = new ObjectMapper();
      String json = objectMapper.writeValueAsString(sensor.getRegisterMap().getData());
      return json.getBytes();
    }
    return jsonObject.toString(2).getBytes();
  }

  public byte[] getDeviceState() throws IOException {
    JSONObject jsonObject = new JSONObject();
    if (sensor != null) {
      List<SensorReading<?>> readings = sensor.getReadings();
      for (SensorReading<?> reading : readings) {
        ComputationResult<?> computationResult = reading.getValue();
        if (!computationResult.hasError()) {
          jsonObject.put(reading.getName(), computationResult.getResult());
        } else {
          jsonObject.put(reading.getName(), computationResult.getError().getMessage());
        }
      }
    }
    return jsonObject.toString(2).getBytes();
  }

  @Override
  public byte[] updateDeviceConfiguration(byte[] val) throws IOException {
    if (sensor != null) {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, AbstractRegisterData.class);
      List<AbstractRegisterData> data = objectMapper.readValue(new String(val), type);
      sensor.getRegisterMap().setData(data);
    }
    return ("{}").getBytes();
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig(buildSchema());
    config.setComments(description);
    config.setSource("I2C bus address : 0x23");
    config.setVersion("1.0");
    config.setResourceType("sensor");
    config.setInterfaceDescription("Returns JSON object containing current lux level");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    return new int[]{0x23};
  }

  private String buildSchema() {
    EnumSchema.Builder resolutionModeEnum = EnumSchema.builder();
    for (ResolutionMode val : ResolutionMode.values()) {
      resolutionModeEnum.possibleValue(val.name());
    }

    EnumSchema.Builder sensorReadingEnum = EnumSchema.builder();
    for (SensorReadingMode val : SensorReadingMode.values()) {
      sensorReadingEnum.possibleValue(val.name());
    }

    ObjectSchema.Builder staticSchema = ObjectSchema.builder()
        .addPropertySchema(RESOLUTION_MODE, resolutionModeEnum.build())
        .addPropertySchema(SENSOR_READING, sensorReadingEnum.build());

    ObjectSchema.Builder updateSchema = ObjectSchema.builder()
        .addPropertySchema("lux",
            NumberSchema.builder()
                .minimum(0)
                .maximum(65535)
                .description("LUX value")
                .build()
        );
    ObjectSchema.Builder schemaBuilder = ObjectSchema.builder();
    schemaBuilder
        .addPropertySchema(NamingConstants.SENSOR_DATA_SCHEMA, updateSchema.build())
        .addPropertySchema(NamingConstants.DEVICE_STATIC_DATA_SCHEMA, staticSchema.build())
        .addPropertySchema(NamingConstants.DEVICE_WRITE_SCHEMA, staticSchema.build())
        .description(description)
        .title(name);

    ObjectSchema schema = schemaBuilder.build();
    return schemaToString(schema);
  }
}
