/*
 * Copyright [ 2024 - 2026 ] MapsMessaging B.V.
 * Licensed under the Apache License, Version 2.0 with the Commons Clause.
 */
package io.mapsmessaging.devices.serial.devices.sensors.sen0609;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.serial.ComputedSerialDeviceController;
import io.mapsmessaging.devices.serial.SerialDeviceController;
import io.mapsmessaging.devices.serial.devices.sensors.SerialDevice;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** Independent change FIFO view, without serial-port or acquisition-thread ownership. */
public final class C4001ChangeController extends ComputedSerialDeviceController {

  private final C4001Sensor sensor;

  public C4001ChangeController(C4001Sensor sensor) {
    this.sensor = sensor;
  }

  @Override public String getComputedDeviceSuffix() { return "change"; }
  @Override public String getName() { return "SEN0609_change"; }
  @Override public String getDescription() { return "Significant C4001 radar observation changes"; }
  @Override public DeviceType getType() { return DeviceType.SENSOR; }

  @Override
  public SerialDeviceController mount(SerialDevice device) throws IOException {
    throw new IOException("Computed radar device cannot mount physical hardware");
  }

  @Override
  public SchemaConfig getSchema() {
    JsonObject root = new JsonObject();
    root.addProperty("type", "object");
    JsonObject props = new JsonObject();
    JsonObject hasData = new JsonObject();
    hasData.addProperty("type", "boolean");
    props.add("hasData", hasData);
    JsonObject change = new JsonObject();
    change.addProperty("type", "object");
    JsonObject fields = new JsonObject();
    JsonObject stamp = new JsonObject();
    stamp.addProperty("type", "string");
    stamp.addProperty("format", "date-time");
    fields.add("previousTimestamp", stamp.deepCopy());
    fields.add("timestamp", stamp.deepCopy());
    JsonObject changes = new JsonObject();
    changes.addProperty("type", "array");
    JsonObject item = new JsonObject();
    item.addProperty("type", "string");
    changes.add("items", item);
    fields.add("changedFields", changes);
    JsonObject observation = new JsonObject();
    observation.addProperty("type", "object");
    fields.add("previous", observation.deepCopy());
    fields.add("current", observation.deepCopy());
    change.add("properties", fields);
    props.add("change", change);
    root.add("properties", props);
    root.addProperty("additionalProperties", false);
    JsonSchemaConfig schema = new JsonSchemaConfig(root.toString());
    schema.setTitle(getName());
    schema.setComments(getDescription());
    schema.setDescription(getDescription());
    schema.setResourceType("sensor");
    schema.setVersion(1);
    schema.setUniqueId(getSchemaId());
    return schema;
  }

  @Override
  protected byte[] readComputedState() {
    C4001Change change = sensor.changes().getData();
    JsonObject result = new JsonObject();
    result.addProperty("hasData", change != null);
    if (change != null) result.add("change", gson.toJsonTree(change));
    return convert(result);
  }

  @Override
  public byte[] getConfigEventDescription() {
    JsonObject definition = new JsonObject();
    definition.addProperty("device", getName());
    definition.addProperty("description", getDescription());
    JsonObject commands = new JsonObject();
    addCommand(commands, "SET_DISTANCE_THRESHOLD", "metres", "Distance threshold (metres)");
    addCommand(commands, "SET_VELOCITY_THRESHOLD", "metresPerSecond", "Velocity threshold (metres/second)");
    definition.add("commands", commands);
    definition.addProperty("readOnly", false);
    return convert(definition);
  }

  private static void addCommand(JsonObject commands, String name, String parameter, String description) {
    JsonObject operation = new JsonObject();
    operation.addProperty("description", description);
    JsonObject parameters = new JsonObject();
    parameters.addProperty("type", "object");
    JsonObject props = new JsonObject();
    JsonObject value = new JsonObject();
    value.addProperty("type", "number");
    value.addProperty("exclusiveMinimum", 0);
    value.addProperty("maximum", 100);
    props.add(parameter, value);
    parameters.add("properties", props);
    JsonArray required = new JsonArray();
    required.add(parameter);
    parameters.add("required", required);
    parameters.addProperty("additionalProperties", false);
    operation.add("parameters", parameters);
    commands.add(name, operation);
  }

  @Override
  protected byte[] updateComputedConfiguration(byte[] payload) throws IOException {
    try {
      JsonObject request = JsonParser.parseString(new String(payload, StandardCharsets.UTF_8)).getAsJsonObject();
      if (!request.has("command") || request.size() != 2) throw new IOException("Invalid C4001 change command");
      String command = request.get("command").getAsString();
      double value;
      switch (command) {
        case "SET_DISTANCE_THRESHOLD" -> {
          value = request.get("metres").getAsDouble();
          sensor.differ().setDistanceThresholdMetres(value);
        }
        case "SET_VELOCITY_THRESHOLD" -> {
          value = request.get("metresPerSecond").getAsDouble();
          sensor.differ().setVelocityThresholdMetresPerSecond(value);
        }
        default -> throw new IOException("Unknown C4001 change command: " + command);
      }
      JsonObject response = new JsonObject();
      response.addProperty("status", "success");
      return convert(response);
    } catch (IllegalStateException | UnsupportedOperationException | NullPointerException
             | com.google.gson.JsonParseException | IllegalArgumentException exception) {
      throw new IOException("Invalid radar change configuration", exception);
    }
  }
}
