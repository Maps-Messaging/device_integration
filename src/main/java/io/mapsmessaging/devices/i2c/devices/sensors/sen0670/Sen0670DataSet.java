/*
 * Copyright (c) 2026 MapsMessaging B.V. Licensed under the project's license.
 */
package io.mapsmessaging.devices.i2c.devices.sensors.sen0670;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/** One complete SEN0670 inference cycle, published only after its end frame is received. */
public record Sen0670DataSet(Instant timestamp, int model, List<Sen0670Protocol.Detection> detections) {
  public Sen0670DataSet {
    Objects.requireNonNull(timestamp, "timestamp");
    detections = List.copyOf(detections);
  }
}
