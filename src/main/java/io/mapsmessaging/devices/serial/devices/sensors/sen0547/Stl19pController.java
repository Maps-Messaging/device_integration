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
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.serial.SerialDeviceController;
import io.mapsmessaging.devices.serial.devices.sensors.SerialDevice;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import java.io.IOException;

public class Stl19pController extends SerialDeviceController {

  private static final String DESCRIPTION = "description";
  private static final String MINIMUM = "minimum";
  private static final String MAXIMUM = "maximum";

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
  public byte[] getDeviceConfiguration() throws IOException {
    return getConfigEventDescription();
  }

  @Override
  public DeviceType getType() {
    return DeviceType.SENSOR;
  }

  @Override
  public byte[] getDeviceState() throws IOException {
    Stl19pSensor.Scan scan = sensor.getData();
    if (scan == null) {
      JsonObject unavailable = new JsonObject();
      unavailable.addProperty("hasData", false);
      return convert(unavailable);
    }
    JsonObject result = gson.toJsonTree(scan).getAsJsonObject();
    result.addProperty("hasData", true);
    return convert(result);
  }

  @Override
  public byte[] getConfigEventDescription() throws IOException {
    JsonObject contract = new JsonObject();
    contract.addProperty("device", getName());
    contract.addProperty("description", getDescription());
    JsonObject commands = new JsonObject();
    JsonObject command = new JsonObject();
    command.addProperty("description", "Configure minimum meaningful LiDAR distance change");
    JsonObject parameters = new JsonObject();
    parameters.addProperty("type", "object");
    JsonObject properties = new JsonObject();
    JsonObject distance = new JsonObject();
    distance.addProperty("type", "integer");
    distance.addProperty("minimum", 1);
    distance.addProperty("maximum", 65535);
    properties.add("thresholdMm", distance);
    parameters.add("properties", properties);
    JsonArray required = new JsonArray();
    required.add("thresholdMm");
    parameters.add("required", required);
    parameters.addProperty("additionalProperties", false);
    command.add("parameters", parameters);
    commands.add("SET_CHANGE_THRESHOLD", command);
    contract.add("commands", commands);
    contract.addProperty("readOnly", false);
    return convert(contract);
  }

  @Override
  public byte[] updateDeviceConfiguration(byte[] val) throws IOException {
    JsonObject request;
    try {
      request = JsonParser.parseString(new String(val, StandardCharsets.UTF_8)).getAsJsonObject();
    } catch (IllegalStateException | com.google.gson.JsonParseException exception) {
      return "{}".getBytes(StandardCharsets.UTF_8);
    }
    if (!request.has("command")) {
      return "{}".getBytes(StandardCharsets.UTF_8);
    }
    if (request.size() != 2 || !request.has("thresholdMm")
        || !request.get("command").isJsonPrimitive()
        || !request.get("command").getAsJsonPrimitive().isString()
        || !"SET_CHANGE_THRESHOLD".equals(request.get("command").getAsString())
        || !request.get("thresholdMm").isJsonPrimitive()
        || !request.get("thresholdMm").getAsJsonPrimitive().isNumber()) {
      throw new IOException("Invalid STL-19P change-threshold command");
    }
    int threshold;
    try {
      threshold = request.get("thresholdMm").getAsBigDecimal().intValueExact();
      sensor.setChangeThresholdMm(threshold);
    } catch (ArithmeticException | IllegalArgumentException exception) {
      throw new IOException("Invalid LiDAR change threshold", exception);
    }
    JsonObject response = new JsonObject();
    response.addProperty("status", "success");
    response.addProperty("thresholdMm", threshold);
    return convert(response);
  }

  public byte[] getChangeState() throws IOException {
    Stl19pScanDiffer.ScanChange change = sensor.changes().getData();
    JsonObject result = new JsonObject();
    result.addProperty("hasData", change != null);
    if (change != null) {
      result.add("change", gson.toJsonTree(change));
    }
    return convert(result);
  }

  public int getChangeThresholdMm() {
    return sensor.getChangeThresholdMm();
  }

  public void setChangeThresholdMm(int thresholdMm) {
    sensor.setChangeThresholdMm(thresholdMm);
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

    JsonObject timestamp = new JsonObject();
    timestamp.addProperty("type", "string");
    timestamp.addProperty("format", "date-time");
    timestamp.addProperty(DESCRIPTION, "ISO 8601 UTC timestamp at the start of scan acquisition");
    timestamp.addProperty("readOnly", true);
    properties.add("timestamp", timestamp);
    JsonObject available = new JsonObject();
    available.addProperty("type", "boolean");
    properties.add("hasData", available);

    JsonObject points = new JsonObject();
    points.addProperty("type", "array");
    points.addProperty(DESCRIPTION, "Polar range samples ordered in scan acquisition order");
    points.add("items", buildPointSchema());
    properties.add("points", points);

    root.add("properties", properties);
    // An empty FIFO produces only hasData=false, so scan fields are optional.
    root.addProperty("additionalProperties", false);
    return gson.toJson(root);
  }

  private JsonObject buildPointSchema() {
    JsonObject point = new JsonObject();
    point.addProperty("type", "object");

    JsonObject properties = new JsonObject();

    JsonObject angle = new JsonObject();
    angle.addProperty("type", "number");
    angle.addProperty(MINIMUM, 0);
    angle.addProperty(MAXIMUM, 360);
    angle.addProperty(DESCRIPTION, "Bearing in degrees");
    properties.add("angleDegrees", angle);

    JsonObject distance = new JsonObject();
    distance.addProperty("type", "integer");
    distance.addProperty(MINIMUM, 0);
    distance.addProperty(MAXIMUM, 65535);
    distance.addProperty(DESCRIPTION, "Distance in millimetres");
    properties.add("distanceMm", distance);

    JsonObject intensity = new JsonObject();
    intensity.addProperty("type", "integer");
    intensity.addProperty(MINIMUM, 0);
    intensity.addProperty(MAXIMUM, 255);
    intensity.addProperty(DESCRIPTION, "Return intensity");
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
