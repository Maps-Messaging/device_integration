/*
 * Copyright (c) 2026 MapsMessaging B.V. Licensed under the project's license.
 */
package io.mapsmessaging.devices.i2c.devices.sensors.sen0670;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** SEN0670 controller: only cached complete inference batches are returned to applications. */
public final class Sen0670Controller extends I2CDeviceController {
  private final Sen0670Sensor sensor;

  public Sen0670Controller() { sensor = null; }

  public Sen0670Controller(AddressableDevice device) throws IOException {
    super(device);
    sensor = new Sen0670Sensor(device);
  }

  @Override public String getName() { return "SEN0670"; }
  @Override public String getDescription() { return "DFRobot Human Pose, Hand and Gesture sensor"; }
  @Override public DeviceType getType() { return DeviceType.SENSOR; }
  @Override public I2CDevice getDevice() { return sensor; }
  @Override public int[] getAddressRange() { return new int[]{Sen0670Protocol.ADDRESS}; }
  @Override public boolean canDetect() { return true; }

  @Override public boolean detect(AddressableDevice device) {
    try {
      new Sen0670Protocol(device).initialize();
      return true;
    } catch (IOException exception) {
      return false;
    }
  }

  @Override public I2CDeviceController mount(AddressableDevice device) throws IOException {
    return new Sen0670Controller(device);
  }

  @Override public SchemaConfig getSchema() {
    JsonObject schema = new JsonObject();
    schema.addProperty("type", "object");
    JsonObject properties = new JsonObject();
    properties.add("hasData", typed("boolean"));
    JsonObject timestamp = typed("string");
    timestamp.addProperty("format", "date-time");
    properties.add("timestamp", timestamp);
    properties.add("model", typed("integer"));
    JsonObject detections = typed("array");
    JsonObject item = typed("object");
    JsonObject fields = new JsonObject();
    for (String key : List.of("id", "x", "y", "width", "height", "score")) {
      fields.add(key, typed("integer"));
    }
    fields.add("name", typed("string"));
    JsonObject keypoints = typed("array");
    JsonObject point = typed("object");
    JsonObject coordinates = new JsonObject();
    coordinates.add("x", typed("integer"));
    coordinates.add("y", typed("integer"));
    point.add("properties", coordinates);
    JsonArray options = new JsonArray();
    options.add(point);
    options.add(typed("null"));
    JsonObject pointItem = new JsonObject();
    pointItem.add("oneOf", options);
    keypoints.add("items", pointItem);
    fields.add("keypoints", keypoints);
    item.add("properties", fields);
    detections.add("items", item);
    properties.add("detections", detections);
    schema.add("properties", properties);
    schema.addProperty("additionalProperties", false);

    JsonSchemaConfig config = new JsonSchemaConfig(schema.toString());
    config.setTitle(getName());
    config.setComments(getDescription());
    config.setResourceType("sensor");
    config.setVersion(1);
    config.setUniqueId(getSchemaId());
    config.setDescription("Timestamped coherent SEN0670 detection batches");
    return config;
  }

  private static JsonObject typed(String type) {
    JsonObject result = new JsonObject();
    result.addProperty("type", type);
    return result;
  }

  @Override public byte[] getDeviceState() throws IOException {
    if (sensor == null) throw new IOException("SEN0670 is not mounted");
    Sen0670DataSet batch = sensor.getData();
    JsonObject state = new JsonObject();
    state.addProperty("hasData", batch != null);
    if (batch == null) return state.toString().getBytes(StandardCharsets.UTF_8);
    state.addProperty("timestamp", batch.timestamp().toString());
    state.addProperty("model", batch.model());
    JsonArray detections = new JsonArray();
    for (Sen0670Protocol.Detection detection : batch.detections()) {
      JsonObject result = new JsonObject();
      result.addProperty("id", detection.id());
      result.addProperty("name", detection.name());
      result.addProperty("x", detection.x());
      result.addProperty("y", detection.y());
      result.addProperty("width", detection.width());
      result.addProperty("height", detection.height());
      result.addProperty("score", detection.score());
      JsonArray points = new JsonArray();
      for (Sen0670Protocol.Point point : detection.keypoints()) {
        if (point == null) {
          points.add((JsonElement) null);
        } else {
          JsonObject value = new JsonObject();
          value.addProperty("x", point.x());
          value.addProperty("y", point.y());
          points.add(value);
        }
      }
      result.add("keypoints", points);
      detections.add(result);
    }
    state.add("detections", detections);
    return state.toString().getBytes(StandardCharsets.UTF_8);
  }

  @Override public byte[] getConfigEventDescription() {
    JsonObject description = new JsonObject();
    description.addProperty("device", getName());
    description.addProperty("description", getDescription());
    JsonObject commands = new JsonObject();
    for (String command : List.of("SET_MODEL", "SET_CONFIDENCE", "SET_IOU", "SET_SIMILARITY",
        "SET_KEYPOINT_OUTPUT", "GET_LEARNED_NAMES")) {
      JsonObject metadata = new JsonObject();
      metadata.addProperty("description", command.replace('_', ' ').toLowerCase());
      JsonObject parameters = typed("object");
      JsonObject properties = new JsonObject();
      JsonObject value = typed("integer");
      if ("SET_MODEL".equals(command)) {
        JsonArray options = new JsonArray();
        options.add(1); options.add(3); options.add(4);
        value.add("enum", options);
      } else if ("GET_LEARNED_NAMES".equals(command)) {
        JsonArray options = new JsonArray();
        options.add(1); options.add(3);
        value.add("enum", options);
      } else {
        value.addProperty("minimum", 0);
        value.addProperty("maximum", "SET_KEYPOINT_OUTPUT".equals(command) ? 1 : 100);
      }
      properties.add("value", value);
      parameters.add("properties", properties);
      JsonArray required = new JsonArray();
      required.add("value");
      parameters.add("required", required);
      parameters.addProperty("additionalProperties", false);
      metadata.add("parameters", parameters);
      commands.add(command, metadata);
    }
    description.add("commands", commands);
    description.addProperty("readOnly", false);
    return description.toString().getBytes(StandardCharsets.UTF_8);
  }

  @Override public byte[] executeConfigCommand(byte[] payload) throws IOException {
    if (sensor == null) throw new IOException("SEN0670 is not mounted");
    try {
      JsonObject request = JsonParser.parseString(new String(payload, StandardCharsets.UTF_8)).getAsJsonObject();
      if (request.size() != 2 || !request.has("command") || !request.has("value")
          || !request.get("command").isJsonPrimitive() || !request.get("command").getAsJsonPrimitive().isString()
          || !request.get("value").isJsonPrimitive() || !request.get("value").getAsJsonPrimitive().isNumber()) {
        throw new IOException("SEN0670 request requires command and integer value");
      }
      String command = request.get("command").getAsString();
      int value = request.get("value").getAsBigDecimal().intValueExact();
      sensor.request(command, value);
      return "{\"status\":\"requested\"}".getBytes(StandardCharsets.UTF_8);
    } catch (IllegalStateException | UnsupportedOperationException | ArithmeticException exception) {
      throw new IOException("Invalid SEN0670 configuration request", exception);
    }
  }
}
