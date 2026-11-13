/*
 * Copyright [ 2024 - 2026 ] MapsMessaging B.V.
 * Licensed under the Apache License, Version 2.0 with the Commons Clause.
 */
package io.mapsmessaging.devices;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Validates a primary's computed endpoints before registering any of them. */
public final class ComputedDeviceRegistration {

  private ComputedDeviceRegistration() {}

  public static <T extends DeviceController> Map<String, T> collect(
      DeviceController primary, String primaryKey, Class<T> type, Map<String, ?> existing) throws IOException {
    Objects.requireNonNull(primary, "primary");
    Objects.requireNonNull(primaryKey, "primaryKey");
    Map<String, T> computed = new LinkedHashMap<>();
    if (!primary.hasComputedDevices()) return computed;
    List<? extends DeviceController> candidates = primary.getComputedDevices();
    if (candidates == null) throw new IOException("Computed device list must not be null");
    for (DeviceController candidate : candidates) {
      if (candidate == null || candidate == primary || !type.isInstance(candidate)) {
        throw new IOException("Invalid computed controller for " + primary.getName());
      }
      String suffix = candidate.getComputedDeviceSuffix();
      if (suffix == null || !suffix.matches("[A-Za-z][A-Za-z0-9_-]*")) {
        throw new IOException("Invalid computed device suffix: " + suffix);
      }
      String key = primaryKey + "_" + suffix;
      String name = primary.getName() + "_" + suffix;
      if (!name.equals(candidate.getName())) {
        throw new IOException("Computed device name must be " + name);
      }
      if (existing.containsKey(key) || computed.containsKey(key)) {
        throw new IOException("Duplicate computed device key: " + key);
      }
      computed.put(key, type.cast(candidate));
    }
    return computed;
  }
}
