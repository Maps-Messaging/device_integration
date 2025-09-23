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

package io.mapsmessaging.devices;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.i2c.devices.rtc.ds3231.LocalDateTimeSensorReading;
import io.mapsmessaging.devices.sensorreadings.*;

import java.util.List;

public class SchemaBuilder {

  private static final String TYPE = "type";
  private static final String OBJECT="object";
  private static final String DESCRIPTION="description";
  private static final String PROPERTIES="properties";
  private static final String REQUIRED="required";
  private static final String SCHEMA = "$schema";
  private static final String STRING = "string";
  private static final String READ_ONLY = "readOnly";
  private static final String UNIT = "Unit: ";

  private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  public static String buildSchema(Sensor sensor, String name, String description) {
    JsonObject staticSchema = new JsonObject();
    staticSchema.addProperty(TYPE, OBJECT);
    return buildSchema(sensor, staticSchema, name, description);
  }

  public static String buildSchema(Sensor sensor, JsonObject staticSchema, String name, String description) {
    JsonObject sensorSchema = buildSchemaFromReadings(sensor.getReadings());

    JsonObject fullSchema = new JsonObject();
    fullSchema.addProperty(SCHEMA, "https://json-schema.org/draft/2020-12/schema");
    fullSchema.addProperty("title", name);
    fullSchema.addProperty(DESCRIPTION, description);
    fullSchema.addProperty(TYPE, OBJECT);

    JsonObject properties = new JsonObject();
    properties.add(NamingConstants.DEVICE_STATIC_DATA_SCHEMA, staticSchema);
    properties.add(NamingConstants.SENSOR_DATA_SCHEMA, sensorSchema);

    fullSchema.add(PROPERTIES, properties);
    fullSchema.add(REQUIRED, gson.toJsonTree(List.of(
        NamingConstants.DEVICE_STATIC_DATA_SCHEMA,
        NamingConstants.SENSOR_DATA_SCHEMA
    )));
    fullSchema.addProperty("additionalProperties", true);
    return gson.toJson(fullSchema);
  }

  private static JsonObject buildSchemaFromReadings(List<SensorReading<?>> readings) {
    JsonObject schema = new JsonObject();
    schema.addProperty(SCHEMA, "https://json-schema.org/draft/2020-12/schema");
    schema.addProperty(TYPE, OBJECT);

    JsonObject properties = new JsonObject();
    JsonArray required = new JsonArray();

    processSchemaList(properties, required, readings);

    schema.add(PROPERTIES, properties);
    schema.add(REQUIRED, required);
    schema.addProperty("additionalProperties", true);
    return schema;
  }

  private static void processSchemaList(JsonObject properties, JsonArray required, List<SensorReading<?>> readings) {
    for (SensorReading<?> reading : readings) {
      JsonObject prop;

      if (reading instanceof GroupSensorReading groupSensorReading) {
        prop = new JsonObject();
        processSchemaList(prop, required, groupSensorReading.getGroupList());
      } else {
        prop = buildReadingSchema(reading);
      }

      properties.add(reading.getName(), prop);
      if (!(reading instanceof OptionalBooleanSensorReading)) {
        required.add(reading.getName());
      }
    }

    // Add timestamp once
    JsonObject timestampProp = new JsonObject();
    timestampProp.addProperty(TYPE, STRING);
    timestampProp.addProperty("format", "date-time");
    timestampProp.addProperty(DESCRIPTION, "Optional ISO 8601 UTC timestamp (e.g., 2025-05-29T07:28:15.123Z)");
    timestampProp.addProperty(READ_ONLY, true);
    properties.add("timestamp", timestampProp);
  }

  private static JsonObject buildReadingSchema(SensorReading<?> reading) {
    JsonObject prop = new JsonObject();
    if (reading instanceof FloatSensorReading floatReading) {
      handleFloatRegister(prop, floatReading);
    } else if (reading instanceof IntegerSensorReading || reading instanceof LongSensorReading) {
      handleLongRegister(prop, (NumericSensorReading<?>) reading);
    } else if (reading instanceof StringSensorReading) {
      prop.addProperty(TYPE, STRING);
      prop.addProperty(DESCRIPTION, UNIT + reading.getUnit());
    } else if (reading instanceof OrientationSensorReading orientationReading) {
      handleOrientationRegister(prop, orientationReading);
    } else if (reading instanceof LocalDateTimeSensorReading dateTime) {
      handleDateTimeRegister(prop, dateTime);
    } else if (reading instanceof BooleanSensorReading || reading instanceof OptionalBooleanSensorReading) {
      handleBooleanRegister(prop, reading);
    } else {
      // fallback: unknown type
      prop.addProperty(TYPE, STRING);
      prop.addProperty(DESCRIPTION, UNIT + reading.getUnit());
    }
    return prop;
  }

  private static void handleBooleanRegister(JsonObject prop, SensorReading<?> reading) {
    prop.addProperty(TYPE, "boolean");
    prop.addProperty(DESCRIPTION, UNIT + reading.getUnit());
    prop.addProperty(READ_ONLY, reading.isReadOnly());
  }

  private static void handleDateTimeRegister(JsonObject prop, LocalDateTimeSensorReading reading) {
    prop.addProperty(TYPE, STRING);
    prop.addProperty("format", "date-time");
    prop.addProperty(DESCRIPTION, reading.getDescription() != null ? reading.getDescription() : UNIT + reading.getUnit());
    if (reading.getExample() != null) {
      prop.add("examples", gson.toJsonTree(List.of(reading.getExample().toString())));
    }
    prop.addProperty(READ_ONLY, reading.isReadOnly());
  }

  private static void handleOrientationRegister(JsonObject prop, OrientationSensorReading orientation) {
    prop.addProperty(TYPE, OBJECT);
    JsonObject orientationProps = new JsonObject();
    for (String axis : List.of("x", "y", "z")) {
      JsonObject axisProp = new JsonObject();
      axisProp.addProperty(TYPE, "number");
      orientationProps.add(axis, axisProp);
    }
    prop.add(PROPERTIES, orientationProps);
    prop.add(REQUIRED, gson.toJsonTree(List.of("x", "y", "z")));
    prop.addProperty(DESCRIPTION, UNIT + orientation.getUnit());
  }

  private static void handleFloatRegister(JsonObject prop, FloatSensorReading floatReading){
    prop.addProperty(TYPE, "number");
    prop.addProperty("minimum", floatReading.getMinimum());
    prop.addProperty("maximum", floatReading.getMaximum());
    prop.addProperty("x-precision", floatReading.getPrecision());
    prop.addProperty(DESCRIPTION, UNIT + floatReading.getUnit());
  }

  private static void handleLongRegister(JsonObject prop, NumericSensorReading<?> num){
    prop.addProperty(TYPE, "integer");
    prop.addProperty("minimum", num.getMinimum().longValue());
    prop.addProperty("maximum", num.getMaximum().longValue());
    prop.addProperty(DESCRIPTION, UNIT + num.getUnit());
  }

  private SchemaBuilder(){
    //no op
  }
}