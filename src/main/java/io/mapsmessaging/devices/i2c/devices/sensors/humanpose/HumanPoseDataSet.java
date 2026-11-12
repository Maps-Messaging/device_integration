/*
 * Copyright (c) 2026 MapsMessaging B.V. Licensed under the project's license.
 */
package io.mapsmessaging.devices.i2c.devices.sensors.humanpose;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/** One complete SEN0670 inference cycle, published only after its end frame is received. */
public record HumanPoseDataSet(Instant timestamp, int model, List<HumanPoseProtocol.Detection> detections) {
  public HumanPoseDataSet {
    Objects.requireNonNull(timestamp, "timestamp");
    detections = List.copyOf(detections);
  }
}
