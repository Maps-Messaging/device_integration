/*
 * Copyright [ 2024 - 2026 ] MapsMessaging B.V.
 * Licensed under the Apache License, Version 2.0 with the Commons Clause.
 */
package io.mapsmessaging.devices.serial.devices.sensors.sen0609;

import java.time.Instant;

/** One coherent observation from a single complete radar report. Null indicates an unavailable measurement. */
public record C4001Observation(Instant timestamp, Mode mode, boolean present,
                               Integer targets, Double distanceMetres, Double velocityMetresPerSecond,
                               Integer energy) {
  public enum Mode { PRESENCE, SPEED }
}
