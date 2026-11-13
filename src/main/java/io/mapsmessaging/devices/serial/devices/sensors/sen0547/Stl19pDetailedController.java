/*
 * Copyright [ 2024 - 2026 ] MapsMessaging B.V.
 * Licensed under the Apache License, Version 2.0 with the Commons Clause.
 */
package io.mapsmessaging.devices.serial.devices.sensors.sen0547;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.serial.ComputedSerialDeviceController;
import io.mapsmessaging.devices.serial.SerialDeviceController;
import io.mapsmessaging.devices.serial.devices.sensors.SerialDevice;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import java.io.IOException;

/** Detailed, sorted polar record from seven complete revolutions of the shared LiDAR. */
public final class Stl19pDetailedController extends ComputedSerialDeviceController {

  private final Stl19pSensor sensor;

  public Stl19pDetailedController(Stl19pSensor sensor) {
    this.sensor = sensor;
  }

  @Override public String getComputedDeviceSuffix() { return "detailed"; }
  @Override public String getName() { return "SEN0547_detailed"; }
  @Override public String getDescription() { return "Sorted STL-19P measurements from seven complete revolutions"; }
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

    JsonObject timestamp = new JsonObject();
    timestamp.addProperty("type", "string");
    timestamp.addProperty("format", "date-time");
    timestamp.addProperty("description", "ISO 8601 UTC timestamp at the start of the seven-revolution record");
    properties.add("timestamp", timestamp);

    JsonObject points = new JsonObject();
    points.addProperty("type", "array");
    points.addProperty("description", "All measurements from seven revolutions, sorted by angle; duplicates preserved");
    JsonObject point = new JsonObject();
    point.addProperty("type", "object");
    JsonObject fields = new JsonObject();
    JsonObject angle = new JsonObject();
    angle.addProperty("type", "number");
    angle.addProperty("minimum", 0);
    angle.addProperty("maximum", 359.99);
    angle.addProperty("multipleOf", 0.01);
    angle.addProperty("description", "Bearing in degrees, rounded to two decimal places");
    fields.add("angle", angle);
    JsonObject distance = new JsonObject();
    distance.addProperty("type", "integer");
    distance.addProperty("minimum", 0);
    distance.addProperty("maximum", 65535);
    distance.addProperty("description", "Distance in millimetres");
    fields.add("distance", distance);
    JsonObject intensity = new JsonObject();
    intensity.addProperty("type", "integer");
    intensity.addProperty("minimum", 0);
    intensity.addProperty("maximum", 255);
    intensity.addProperty("description", "Return intensity, original 8-bit measurement");
    fields.add("intensity", intensity);
    point.add("properties", fields);
    JsonArray required = new JsonArray();
    required.add("angle");
    required.add("distance");
    required.add("intensity");
    point.add("required", required);
    point.addProperty("additionalProperties", false);
    points.add("items", point);
    properties.add("points", points);

    root.add("properties", properties);
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
    Stl19pSensor.Scan scan = sensor.detailed().getData();
    JsonObject result = new JsonObject();
    result.addProperty("hasData", scan != null);
    if (scan != null) {
      result.addProperty("timestamp", scan.getTimestamp());
      JsonArray points = new JsonArray();
      for (Stl19pSensor.Point sample : scan.getPoints()) {
        JsonObject point = new JsonObject();
        point.addProperty("angle", sample.getAngleDegrees());
        point.addProperty("distance", sample.getDistanceMm());
        point.addProperty("intensity", sample.getIntensity());
        points.add(point);
      }
      result.add("points", points);
    }
    return convert(result);
  }

  @Override
  public byte[] getConfigEventDescription() {
    JsonObject config = new JsonObject();
    config.addProperty("device", getName());
    config.addProperty("description", getDescription());
    config.add("commands", new JsonObject());
    config.addProperty("readOnly", true);
    return convert(config);
  }
}
