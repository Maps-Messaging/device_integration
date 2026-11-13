/*
 * Copyright [ 2024 - 2026 ] MapsMessaging B.V.
 * Licensed under the Apache License, Version 2.0 with the Commons Clause.
 */
package io.mapsmessaging.devices.serial.devices.sensors.sen0609;

import java.util.ArrayList;
import java.util.List;

/** Compare against the most recently reported significant state, so gradual changes accumulate. */
public final class C4001Differ {
  private volatile double distanceThresholdMetres = 0.15;
  private volatile double velocityThresholdMetresPerSecond = 0.2;

  public void setDistanceThresholdMetres(double value) {
    if (!Double.isFinite(value) || value <= 0 || value > 100) {
      throw new IllegalArgumentException("Distance threshold must be positive and finite");
    }
    distanceThresholdMetres = value;
  }

  public void setVelocityThresholdMetresPerSecond(double value) {
    if (!Double.isFinite(value) || value <= 0 || value > 100) {
      throw new IllegalArgumentException("Velocity threshold must be positive and finite");
    }
    velocityThresholdMetresPerSecond = value;
  }

  public double getDistanceThresholdMetres() { return distanceThresholdMetres; }
  public double getVelocityThresholdMetresPerSecond() { return velocityThresholdMetresPerSecond; }

  public C4001Change diff(C4001Observation previous, C4001Observation current) {
    List<String> changed = new ArrayList<>();
    if (previous.mode() != current.mode()) changed.add("mode");
    if (previous.present() != current.present()) changed.add("presence");
    if (previous.targets() != null && current.targets() != null
        && !previous.targets().equals(current.targets())) changed.add("targets");
    if (previous.mode() == current.mode() && previous.present() && current.present()) {
      if (previous.distanceMetres() != null && current.distanceMetres() != null
          && Math.abs(previous.distanceMetres() - current.distanceMetres()) >= distanceThresholdMetres) {
        changed.add("distanceMetres");
      }
      if (previous.velocityMetresPerSecond() != null && current.velocityMetresPerSecond() != null
          && Math.abs(previous.velocityMetresPerSecond() - current.velocityMetresPerSecond()) >= velocityThresholdMetresPerSecond) {
        changed.add("velocityMetresPerSecond");
      }
    }
    return new C4001Change(previous.timestamp(), current.timestamp(), changed, previous, current);
  }
}
