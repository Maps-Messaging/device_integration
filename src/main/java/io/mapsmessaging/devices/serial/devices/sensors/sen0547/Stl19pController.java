/*
 *
 *  Copyright [ 2020 - 2024 ] Matthew Buckton
 *  Copyright [ 2024 - 2026 ] MapsMessaging B.V.
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

package io.mapsmessaging.devices.serial.devices.sensors.sen0547;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.serial.SerialDeviceController;
import io.mapsmessaging.devices.serial.devices.sensors.SerialDevice;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import java.io.IOException;

public class Stl19pController extends SerialDeviceController {

  private final Stl19pSensor sensor;

  public Stl19pController() {
    sensor = null;
  }

  public Stl19pController(SerialDevice serial) throws IOException {
    sensor = new Stl19pSensor(serial);
  }

  @Override
  public SerialDeviceController mount(SerialDevice serialDevice) throws IOException {
    return new Stl19pController(serialDevice);
  }

  @Override
  public String getName() {
    return "SEN0547";
  }

  @Override
  public String getDescription() {
    return "DFRobot SEN0547 STL-19P 360 degree LiDAR";
  }

  @Override
  public SchemaConfig getSchema() {
    JsonSchemaConfig cfg = new JsonSchemaConfig(buildStl19pSchema());
    cfg.setTitle(getName());
    cfg.setComments(getDescription());
    cfg.setResourceType("sensor");
    cfg.setVersion(1);
    cfg.setUniqueId(getSchemaId());
    cfg.setDescription("Serial 230400 8N1, receive-only");
    return cfg;
  }

  @Override
  public byte[] getDeviceConfiguration() {
    return "{}".getBytes();
  }

  @Override
  public DeviceType getType() {
    return DeviceType.SENSOR;
  }

  @Override
  public byte[] getDeviceState() throws IOException {
    JsonObject jsonObject = new JsonObject();
    walkSensorReadings(jsonObject, sensor.getReadings());
    return convert(jsonObject);
  }

  @Override
  public byte[] updateDeviceConfiguration(byte[] val) {
    return "{}".getBytes();
  }

  @Override
  public void close() {
    if (sensor != null) {
      sensor.close();
    }
  }

  private String buildStl19pSchema() {
    JsonObject root = new JsonObject();
    root.addProperty("type", "object");

    JsonObject properties = new JsonObject();
    properties.add("scan", buildScanSchema());

    JsonObject timestamp = new JsonObject();
    timestamp.addProperty("type", "string");
    timestamp.addProperty("format", "date-time");
    timestamp.addProperty("description", "ISO 8601 UTC timestamp for the completed scan");
    timestamp.addProperty("readOnly", true);
    properties.add("timestamp", timestamp);

    root.add("properties", properties);
    JsonArray required = new JsonArray();
    required.add("scan");
    root.add("required", required);
    return gson.toJson(root);
  }

  private JsonObject buildScanSchema() {
    JsonObject scan = new JsonObject();
    scan.addProperty("type", "object");
    scan.addProperty("description", "One complete 360 degree STL-19P scan");
    scan.addProperty("readOnly", true);

    JsonObject properties = new JsonObject();

    JsonObject frequency = new JsonObject();
    frequency.addProperty("type", "number");
    frequency.addProperty("minimum", 0);
    frequency.addProperty("description", "LiDAR rotation frequency in Hz");
    frequency.addProperty("readOnly", true);
    properties.add("scanFrequencyHz", frequency);

    JsonObject sensorTimestamp = new JsonObject();
    sensorTimestamp.addProperty("type", "integer");
    sensorTimestamp.addProperty("minimum", 0);
    sensorTimestamp.addProperty("maximum", 65535);
    sensorTimestamp.addProperty("description", "Timestamp from the final LiDAR packet in milliseconds");
    sensorTimestamp.addProperty("readOnly", true);
    properties.add("sensorTimestampMs", sensorTimestamp);

    JsonObject points = new JsonObject();
    points.addProperty("type", "array");
    points.addProperty("description", "Polar range samples ordered in scan acquisition order");
    points.add("items", buildPointSchema());
    properties.add("points", points);

    scan.add("properties", properties);
    JsonArray required = new JsonArray();
    required.add("scanFrequencyHz");
    required.add("sensorTimestampMs");
    required.add("points");
    scan.add("required", required);
    return scan;
  }

  private JsonObject buildPointSchema() {
    JsonObject point = new JsonObject();
    point.addProperty("type", "object");

    JsonObject properties = new JsonObject();

    JsonObject angle = new JsonObject();
    angle.addProperty("type", "number");
    angle.addProperty("minimum", 0);
    angle.addProperty("maximum", 360);
    angle.addProperty("description", "Bearing in degrees");
    properties.add("angleDegrees", angle);

    JsonObject distance = new JsonObject();
    distance.addProperty("type", "integer");
    distance.addProperty("minimum", 0);
    distance.addProperty("maximum", 65535);
    distance.addProperty("description", "Distance in millimetres");
    properties.add("distanceMm", distance);

    JsonObject intensity = new JsonObject();
    intensity.addProperty("type", "integer");
    intensity.addProperty("minimum", 0);
    intensity.addProperty("maximum", 255);
    intensity.addProperty("description", "Return intensity");
    properties.add("intensity", intensity);

    point.add("properties", properties);
    JsonArray required = new JsonArray();
    required.add("angleDegrees");
    required.add("distanceMm");
    required.add("intensity");
    point.add("required", required);
    return point;
  }
}
