/*
 *
 *  Copyright [ 2020 - 2024 ] [Matthew Buckton]
 *  Copyright [ 2024 - 2025.  ] [Maps Messaging B.V.]
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package io.mapsmessaging.devices;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.LocalDateTimeSensorReading;
import io.mapsmessaging.devices.sensorreadings.*;
import io.mapsmessaging.devices.util.UuidGenerator;
import io.mapsmessaging.schemas.config.SchemaConfig;
import org.everit.json.schema.ObjectSchema;
import org.everit.json.schema.internal.JSONPrinter;
import org.json.JSONWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public abstract class DeviceController {

  private AtomicReference<UUID> uuid;

  protected DeviceController() {
    uuid = new AtomicReference<>();
  }

  protected static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  public UUID getSchemaId() {
    UUID current = uuid.get();
    if (current == null) {
      UUID newUuid = UuidGenerator.getInstance().generateUuid(getName());
      if (uuid.compareAndSet(null, newUuid)) {
        return newUuid;
      } else {
        return uuid.get(); // someone else set it
      }
    }
    return current;
  }


  public abstract String getName();

  public abstract String getDescription();

  public abstract SchemaConfig getSchema();

  public abstract byte[] getDeviceConfiguration() throws IOException;

  public abstract DeviceType getType();

  public abstract byte[] getDeviceState() throws IOException;

  public boolean getRaiseExceptionOnError() {
    return true;
  }

  public abstract void setRaiseExceptionOnError(boolean flag);

  public byte[] updateDeviceConfiguration(byte[] val) throws IOException {
    return new byte[0];
  }

  public void close() {
  }

  public String schemaToString(ObjectSchema schema) {
    StringWriter stringWriter = new StringWriter();
    JSONWriter jsonWriter = new JSONWriter(stringWriter);
    JSONPrinter printer = new JSONPrinter(jsonWriter);
    schema.describeTo(printer);
    return stringWriter.getBuffer().toString();
  }

  public String buildSchema(Sensor sensor) {
    return buildSchema(sensor, ObjectSchema.builder().build());
  }

  public String buildSchema(Sensor sensor, ObjectSchema staticSchema) {
    JsonObject sensorSchema = buildSchemaFromReadings(sensor.getReadings());

    JsonObject fullSchema = new JsonObject();
    fullSchema.addProperty("$schema", "https://json-schema.org/draft/2020-12/schema");
    fullSchema.addProperty("title", getName());
    fullSchema.addProperty("description", getDescription());
    fullSchema.addProperty("type", "object");

    JsonObject properties = new JsonObject();
    properties.add(NamingConstants.DEVICE_STATIC_DATA_SCHEMA, gson.fromJson(staticSchema.toString(), JsonObject.class));
    properties.add(NamingConstants.SENSOR_DATA_SCHEMA, sensorSchema);

    fullSchema.add("properties", properties);
    fullSchema.add("required", gson.toJsonTree(List.of(
        NamingConstants.DEVICE_STATIC_DATA_SCHEMA,
        NamingConstants.SENSOR_DATA_SCHEMA
    )));
    fullSchema.addProperty("additionalProperties", false);

    return gson.toJson(fullSchema);
  }

  public JsonObject buildSchemaFromReadings(List<SensorReading<?>> readings) {
    JsonObject schema = new JsonObject();
    schema.addProperty("$schema", "https://json-schema.org/draft/2020-12/schema");
    schema.addProperty("type", "object");

    JsonObject properties = new JsonObject();
    JsonArray required = new JsonArray();

    for (SensorReading<?> reading : readings) {
      JsonObject prop = new JsonObject();

      if (reading instanceof FloatSensorReading) {
        FloatSensorReading floatReading = (FloatSensorReading) reading;
        prop.addProperty("type", "number");
        prop.addProperty("minimum", floatReading.getMinimum());
        prop.addProperty("maximum", floatReading.getMaximum());
        prop.addProperty("x-precision", floatReading.getPrecision());
        prop.addProperty("description", "Unit: " + floatReading.getUnit());
      } else if (reading instanceof IntegerSensorReading || reading instanceof LongSensorReading) {
        NumericSensorReading<?> num = (NumericSensorReading<?>) reading;
        prop.addProperty("type", "integer");
        prop.addProperty("minimum", num.getMinimum().longValue());
        prop.addProperty("maximum", num.getMaximum().longValue());
        prop.addProperty("description", "Unit: " + reading.getUnit());
      } else if (reading instanceof StringSensorReading) {
        prop.addProperty("type", "string");
        prop.addProperty("description", "Unit: " + reading.getUnit());
      } else if (reading instanceof OrientationSensorReading) {
        prop.addProperty("type", "object");
        JsonObject orientationProps = new JsonObject();
        for (String axis : List.of("x", "y", "z")) {
          JsonObject axisProp = new JsonObject();
          axisProp.addProperty("type", "number");
          orientationProps.add(axis, axisProp);
        }
        prop.add("properties", orientationProps);
        prop.add("required", new Gson().toJsonTree(List.of("x", "y", "z")));
        prop.addProperty("description", "Unit: " + reading.getUnit());
      } else if (reading instanceof LocalDateTimeSensorReading) {
        prop.addProperty("type", "string");
        prop.addProperty("format", "date-time");
        prop.addProperty("description", reading.getDescription() != null ? reading.getDescription() : "Unit: " + reading.getUnit());

        if (reading.getExample() != null) {
          prop.add("examples", new Gson().toJsonTree(List.of(reading.getExample().toString())));
        }
        prop.addProperty("readOnly", reading.isReadOnly());
      } else {
        // fallback: unknown type
        prop.addProperty("type", "string");
        prop.addProperty("description", "Unit: " + reading.getUnit());
      }

      properties.add(reading.getName(), prop);
      required.add(reading.getName());
    }

    schema.add("properties", properties);
    schema.add("required", required);
    schema.addProperty("additionalProperties", false);
    return schema;
  }
}
