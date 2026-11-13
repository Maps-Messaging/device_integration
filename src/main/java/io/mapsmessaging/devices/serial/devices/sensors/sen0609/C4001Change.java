/*
 * Copyright [ 2024 - 2026 ] MapsMessaging B.V.
 * Licensed under the Apache License, Version 2.0 with the Commons Clause.
 */
package io.mapsmessaging.devices.serial.devices.sensors.sen0609;

import java.time.Instant;
import java.util.List;

/** Comparison of two complete observations; a change FIFO entry is emitted only for non-empty fields. */
public record C4001Change(Instant previousTimestamp, Instant timestamp, List<String> changedFields,
                          C4001Observation previous, C4001Observation current) {
  public C4001Change {
    changedFields = List.copyOf(changedFields);
  }
}
