/*
 * Copyright [ 2024 - 2026 ] MapsMessaging B.V.
 * Licensed under the Apache License, Version 2.0 with the Commons Clause.
 */
package io.mapsmessaging.devices.serial.devices.sensors.sen0547;

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

/** Read-only hardware ownership: all data is obtained from the parent's change FIFO. */
public final class Stl19pChangeController extends ComputedSerialDeviceController {

  private final Stl19pSensor sensor;

  public Stl19pChangeController(Stl19pSensor sensor) {
    this.sensor = sensor;
  }

  @Override public String getComputedDeviceSuffix() { return "change"; }
  @Override public String getName() { return "SEN0547_change"; }
  @Override public String getDescription() { return "Significant changes in complete STL-19P LiDAR scans"; }
  @Override public DeviceType getType() { return DeviceType.SENSOR; }

  @Override
  public SerialDeviceController mount(SerialDevice device) throws IOException {
    throw new IOException("Computed LiDAR device cannot mount physical hardware");
  }

  @Override
  public SchemaConfig getSchema() {
    JsonObject root = new JsonObject();
    root.addProperty("type", "object");
    JsonObject properties = new JsonObject();
    JsonObject available = new JsonObject();
    available.addProperty("type", "boolean");
    properties.add("hasData", available);
    JsonObject change = new JsonObject();
    change.addProperty("type", "object");
    JsonObject changeFields = new JsonObject();
    JsonObject stamp = new JsonObject();
    stamp.addProperty("type", "string");
    stamp.addProperty("format", "date-time");
    changeFields.add("previousTimestamp", stamp.deepCopy());
    changeFields.add("currentTimestamp", stamp.deepCopy());
    JsonObject entries = new JsonObject();
    entries.addProperty("type", "array");
    JsonObject item = new JsonObject();
    item.addProperty("type", "object");
    JsonObject fields = new JsonObject();
    for (String field : new String[]{"angleDegrees", "previousDistanceMm", "currentDistanceMm"}) {
      JsonObject number = new JsonObject();
      number.addProperty("type", "integer");
      fields.add(field, number);
    }
    item.add("properties", fields);
    JsonArray rawRequired = new JsonArray();
    rawRequired.add("angleDegrees");
    rawRequired.add("previousDistanceMm");
    rawRequired.add("currentDistanceMm");
    item.add("required", rawRequired);
    item.addProperty("additionalProperties", false);
    JsonObject mergedItem = new JsonObject();
    mergedItem.addProperty("type", "object");
    JsonObject mergedFields = new JsonObject();
    JsonObject angle = new JsonObject();
    angle.addProperty("type", "number");
    angle.addProperty("minimum", 0);
    angle.addProperty("maximum", 359.9);
    angle.addProperty("multipleOf", 0.1);
    angle.addProperty("description", "Bearing in degrees");
    mergedFields.add("angle", angle);
    for (String field : new String[]{"previousDistance", "distance"}) {
      JsonObject distance = new JsonObject();
      distance.addProperty("type", "integer");
      distance.addProperty("minimum", 0);
      distance.addProperty("maximum", 65535);
      distance.addProperty("description", "Distance in millimetres");
      mergedFields.add(field, distance);
    }
    mergedItem.add("properties", mergedFields);
    JsonArray mergedRequired = new JsonArray();
    mergedRequired.add("angle");
    mergedRequired.add("previousDistance");
    mergedRequired.add("distance");
    mergedItem.add("required", mergedRequired);
    mergedItem.addProperty("additionalProperties", false);
    JsonObject alternatives = new JsonObject();
    JsonArray variants = new JsonArray();
    variants.add(item);
    variants.add(mergedItem);
    alternatives.add("oneOf", variants);
    entries.add("items", alternatives);
    changeFields.add("changes", entries);
    change.add("properties", changeFields);
    properties.add("change", change);
    root.add("properties", properties);
    root.addProperty("additionalProperties", false);
    JsonSchemaConfig schema = new JsonSchemaConfig(root.toString());
    schema.setTitle(getName());
    schema.setComments(getDescription());
    schema.setDescription(getDescription());
    schema.setResourceType("sensor");
    schema.setVersion(2);
    schema.setUniqueId(getSchemaId());
    return schema;
  }

  @Override
  protected byte[] readComputedState() {
    Stl19pScanDiffer.ScanChange change = sensor.changes().getData();
    JsonObject result = new JsonObject();
    result.addProperty("hasData", change != null);
    if (change != null) {
      JsonObject payload = gson.toJsonTree(change).getAsJsonObject();
      if ("merged".equals(sensor.getScanMode())) {
        JsonArray changes = new JsonArray();
        for (Stl19pScanDiffer.BinChange entry : change.changes()) {
          JsonObject point = new JsonObject();
          point.addProperty("angle", entry.angleDegrees() / 10.0);
          point.addProperty("previousDistance", entry.previousDistanceMm());
          point.addProperty("distance", entry.currentDistanceMm());
          changes.add(point);
        }
        payload.add("changes", changes);
      }
      result.add("change", payload);
    }
    return convert(result);
  }

  @Override
  public byte[] getConfigEventDescription() {
    JsonObject definition = new JsonObject();
    definition.addProperty("device", getName());
    definition.addProperty("description", getDescription());
    JsonObject commands = new JsonObject();
    JsonObject operation = new JsonObject();
    operation.addProperty("description", "Set the minimum meaningful LiDAR distance change (millimetres)");
    JsonObject parameters = new JsonObject();
    parameters.addProperty("type", "object");
    JsonObject props = new JsonObject();
    JsonObject threshold = new JsonObject();
    threshold.addProperty("type", "integer");
    threshold.addProperty("minimum", 1);
    threshold.addProperty("maximum", 65535);
    props.add("thresholdMm", threshold);
    parameters.add("properties", props);
    JsonArray required = new JsonArray();
    required.add("thresholdMm");
    parameters.add("required", required);
    parameters.addProperty("additionalProperties", false);
    operation.add("parameters", parameters);
    commands.add("SET_CHANGE_THRESHOLD", operation);
    definition.add("commands", commands);
    definition.addProperty("readOnly", false);
    return convert(definition);
  }

  @Override
  protected byte[] updateComputedConfiguration(byte[] payload) throws IOException {
    try {
      JsonObject request = JsonParser.parseString(new String(payload, StandardCharsets.UTF_8)).getAsJsonObject();
      if (request.size() != 2 || !request.has("command") || !request.has("thresholdMm")
          || !"SET_CHANGE_THRESHOLD".equals(request.get("command").getAsString())) {
        throw new IOException("Invalid LiDAR change configuration command");
      }
      int threshold = request.get("thresholdMm").getAsBigDecimal().intValueExact();
      sensor.setChangeThresholdMm(threshold);
      JsonObject response = new JsonObject();
      response.addProperty("status", "success");
      response.addProperty("thresholdMm", threshold);
      return convert(response);
    } catch (IllegalStateException | UnsupportedOperationException | com.google.gson.JsonParseException
             | ArithmeticException | IllegalArgumentException exception) {
      throw new IOException("Invalid LiDAR change configuration", exception);
    }
  }
}
