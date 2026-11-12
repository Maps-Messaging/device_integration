/*
 * Copyright (c) 2026 MapsMessaging B.V.
 * Licensed under the project's license.
 */
package io.mapsmessaging.devices.queue;

import java.time.Instant;
import java.util.Objects;

/** Completed acquisition, timestamped on publication. The payload must be immutable. */
public record TimedDataSet<T>(Instant timestamp, T data) {
  public TimedDataSet {
    Objects.requireNonNull(timestamp, "timestamp");
    Objects.requireNonNull(data, "data");
  }
}
