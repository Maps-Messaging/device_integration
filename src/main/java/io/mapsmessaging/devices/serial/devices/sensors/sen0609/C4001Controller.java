/*
 * Copyright [ 2024 - 2026 ] MapsMessaging B.V.
 * Licensed under the Apache License, Version 2.0 with the Commons Clause.
 */
package io.mapsmessaging.devices.serial.devices.sensors.sen0609;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.DeviceController;
import java.util.List;
import io.mapsmessaging.devices.serial.SerialDeviceController;
import io.mapsmessaging.devices.serial.devices.sensors.SerialDevice;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** Serial C4001 controller using the same schema/config/state paths as other serial devices. */
public final class C4001Controller extends SerialDeviceController {
  private final C4001Sensor sensor;
  private final C4001ChangeController changeController;

  public C4001Controller() { sensor = null; changeController = null; }
  public C4001Controller(SerialDevice serial) throws IOException {
    sensor = new C4001Sensor(serial);
    changeController = new C4001ChangeController(sensor);
  }

  @Override public SerialDeviceController mount(SerialDevice port) throws IOException { return new C4001Controller(port); }
  @Override public boolean hasComputedDevices() { return changeController != null; }
  @Override public List<? extends DeviceController> getComputedDevices() {
    return changeController == null ? List.of() : List.of(changeController);
  }
  @Override public String getName() { return "SEN0609"; }
  @Override public String getDescription() { return "DFRobot C4001 24 GHz radar (UART 9600 8N1)"; }
  @Override public DeviceType getType() { return DeviceType.SENSOR; }

  @Override public SchemaConfig getSchema() {
    JsonObject root = type("object");
    JsonObject properties = new JsonObject();
    properties.add("hasData", type("boolean"));
    JsonObject timestamp = type("string");
    timestamp.addProperty("format", "date-time");
    properties.add("timestamp", timestamp);
    JsonObject mode = type("string");
    JsonArray enumValues = new JsonArray();
    enumValues.add("PRESENCE");
    enumValues.add("SPEED");
    mode.add("enum", enumValues);
    properties.add("mode", mode);
    properties.add("present", type("boolean"));
    properties.add("targets", nullable("integer"));
    properties.add("distanceMetres", nullable("number"));
    properties.add("velocityMetresPerSecond", nullable("number"));
    properties.add("energy", nullable("integer"));
    root.add("properties", properties);
    root.addProperty("additionalProperties", false);
    JsonSchemaConfig schema = new JsonSchemaConfig(root.toString());
    schema.setTitle(getName());
    schema.setComments(getDescription());
    schema.setResourceType("sensor");
    schema.setVersion(1);
    schema.setUniqueId(getSchemaId());
    schema.setDescription("UART 9600 8N1, timestamped C4001 presence and speed observations");
    return schema;
  }

  private static JsonObject type(String value) {
    JsonObject result = new JsonObject();
    result.addProperty("type", value);
    return result;
  }

  private static JsonObject nullable(String value) {
    JsonObject result = new JsonObject();
    JsonArray options = new JsonArray();
    options.add(value);
    options.add("null");
    result.add("type", options);
    return result;
  }

  @Override public byte[] getDeviceConfiguration() throws IOException { return getConfigEventDescription(); }

  @Override public byte[] getDeviceState() throws IOException {
    requireMounted();
    C4001Observation result = sensor.getData();
    JsonObject state = result == null ? new JsonObject() : gson.toJsonTree(result).getAsJsonObject();
    state.addProperty("hasData", result != null);
    return convert(state);
  }

  /** Separate destructive read from the independently buffered change FIFO. */
  public byte[] getChangeState() throws IOException {
    requireMounted();
    C4001Change change = sensor.changes().getData();
    JsonObject result = new JsonObject();
    result.addProperty("hasData", change != null);
    if (change != null) result.add("change", gson.toJsonTree(change));
    return convert(result);
  }

  public C4001Sensor getSensor() { return sensor; }

  @Override public byte[] getConfigEventDescription() {
    JsonObject result = new JsonObject();
    result.addProperty("device", getName());
    result.addProperty("description", getDescription());
    JsonObject commands = new JsonObject();
    define(commands, "SET_DISTANCE_THRESHOLD", "Minimum distance change in metres", "metres", "number", 0.001, 100);
    define(commands, "SET_VELOCITY_THRESHOLD", "Minimum velocity change in metres per second", "metresPerSecond", "number", 0.001, 100);
    define(commands, "SET_MODE", "Radar firmware operating mode: 0=presence, 1=speed", "mode", "integer", 0, 1);
    define(commands, "SET_TRIGGER_SENSITIVITY", "Firmware presence trigger sensitivity (queued)", "level", "integer", 0, 9);
    define(commands, "SET_KEEP_SENSITIVITY", "Firmware presence retention sensitivity (queued)", "level", "integer", 0, 9);
    result.add("commands", commands);
    result.addProperty("readOnly", false);
    return convert(result);
  }

  private static void define(JsonObject commands, String command, String description,
                             String key, String type, Number min, Number max) {
    JsonObject item = new JsonObject();
    item.addProperty("description", description);
    JsonObject params = type("object");
    JsonObject fields = new JsonObject();
    JsonObject field = type(type);
    field.addProperty("minimum", min);
    field.addProperty("maximum", max);
    fields.add(key, field);
    params.add("properties", fields);
    JsonArray required = new JsonArray();
    required.add(key);
    params.add("required", required);
    params.addProperty("additionalProperties", false);
    item.add("parameters", params);
    commands.add(command, item);
  }

  @Override public byte[] updateDeviceConfiguration(byte[] data) throws IOException {
    requireMounted();
    JsonObject request;
    try {
      request = JsonParser.parseString(new String(data, StandardCharsets.UTF_8)).getAsJsonObject();
      if (!request.has("command") || !request.get("command").isJsonPrimitive()) {
        throw new IOException("Missing C4001 configuration command");
      }
      String command = request.get("command").getAsString();
      if (request.size() != 2) throw new IOException("Unexpected C4001 command parameters");
      switch (command) {
        case "SET_DISTANCE_THRESHOLD" -> sensor.differ().setDistanceThresholdMetres(readDouble(request, "metres"));
        case "SET_VELOCITY_THRESHOLD" -> sensor.differ().setVelocityThresholdMetresPerSecond(readDouble(request, "metresPerSecond"));
        case "SET_MODE" -> sensor.setHardwareMode(readInteger(request, "mode"));
        case "SET_TRIGGER_SENSITIVITY" -> sensor.setHardwareSensitivity(true, readInteger(request, "level"));
        case "SET_KEEP_SENSITIVITY" -> sensor.setHardwareSensitivity(false, readInteger(request, "level"));
        default -> throw new IOException("Unknown C4001 command: " + command);
      }
      JsonObject response = new JsonObject();
      response.addProperty("status", command.endsWith("SENSITIVITY") || command.equals("SET_MODE") ? "requested" : "success");
      return convert(response);
    } catch (IllegalStateException | UnsupportedOperationException | com.google.gson.JsonParseException
             | ArithmeticException | IllegalArgumentException exception) {
      throw new IOException("Invalid C4001 configuration payload", exception);
    }
  }

  private static double readDouble(JsonObject request, String key) throws IOException {
    if (!request.has(key) || !request.get(key).isJsonPrimitive() || !request.get(key).getAsJsonPrimitive().isNumber()) {
      throw new IOException("Missing numeric parameter: " + key);
    }
    return request.get(key).getAsDouble();
  }

  private static int readInteger(JsonObject request, String key) throws IOException {
    if (!request.has(key) || !request.get(key).isJsonPrimitive() || !request.get(key).getAsJsonPrimitive().isNumber()) {
      throw new IOException("Missing integer parameter: " + key);
    }
    return request.get(key).getAsBigDecimal().intValueExact();
  }

  private void requireMounted() throws IOException {
    if (sensor == null) throw new IOException("SEN0609 is not mounted");
  }

  @Override public void close() {
    if (changeController != null) changeController.close();
    if (sensor != null) sensor.close();
  }
}
