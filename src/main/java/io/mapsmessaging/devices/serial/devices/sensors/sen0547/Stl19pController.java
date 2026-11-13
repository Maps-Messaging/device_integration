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
import io.mapsmessaging.devices.DeviceController;
import java.util.List;
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
  private final Stl19pChangeController changeController;
  private final Stl19pDetailedController detailedController;

  public Stl19pController() {
    sensor = null;
    changeController = null;
    detailedController = null;
  }

  public Stl19pController(SerialDevice serial) throws IOException {
    sensor = new Stl19pSensor(serial);
    changeController = new Stl19pChangeController(sensor);
    detailedController = new Stl19pDetailedController(sensor);
  }

  @Override
  public SerialDeviceController mount(SerialDevice serialDevice) throws IOException {
    return new Stl19pController(serialDevice);
  }

  @Override
  public boolean hasComputedDevices() { return changeController != null; }

  @Override
  public List<? extends DeviceController> getComputedDevices() {
    return changeController == null ? List.of() : List.of(detailedController, changeController);
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
    cfg.setVersion(3);
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
    Stl19pSensor.Scan frame = sensor.getData();
    if (frame == null) {
      JsonObject unavailable = new JsonObject();
      unavailable.addProperty("hasData", false);
      return convert(unavailable);
    }
    JsonObject result = gson.toJsonTree(frame).getAsJsonObject();
    result.addProperty("hasData", true);
    return convert(result);
  }

  @Override
  public byte[] getConfigEventDescription() {
    JsonObject contract = new JsonObject();
    contract.addProperty("device", getName());
    contract.addProperty("description", getDescription());
    contract.add("commands", new JsonObject());
    contract.addProperty("readOnly", true);
    return convert(contract);
  }

  @Override
  public byte[] updateDeviceConfiguration(byte[] payload) throws IOException {
    throw new IOException("Raw LiDAR device has no configuration commands");
  }

  @Override
  public void close() {
    if (sensor != null) {
      if (changeController != null) changeController.close();
      if (detailedController != null) detailedController.close();
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
    timestamp.addProperty(DESCRIPTION, "ISO 8601 UTC timestamp when the serial frame was received");
    timestamp.addProperty("readOnly", true);
    properties.add("timestamp", timestamp);
    JsonObject available = new JsonObject();
    available.addProperty("type", "boolean");
    properties.add("hasData", available);

    JsonObject points = new JsonObject();
    points.addProperty("type", "array");
    points.addProperty(DESCRIPTION, "Twelve raw polar measurements from one validated serial frame");
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
