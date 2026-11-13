/*
 * Copyright [ 2024 - 2026 ] MapsMessaging B.V.
 * Licensed under the Apache License, Version 2.0 with the Commons Clause.
 */
package io.mapsmessaging.devices.serial.devices.sensors.sen0547;

import java.util.ArrayList;
import java.util.List;

/** Detects movement by comparing average valid distances in matching one-degree sectors. */
public final class Stl19pScanDiffer {
  public static final int DEFAULT_THRESHOLD_MM = 150;
  private static final int BINS = 360;
  private volatile int thresholdMm;

  public Stl19pScanDiffer(int thresholdMm) {
    setThresholdMm(thresholdMm);
  }

  public void setThresholdMm(int thresholdMm) {
    if (thresholdMm <= 0 || thresholdMm > 65535) {
      throw new IllegalArgumentException("Distance threshold must be 1..65535 mm");
    }
    this.thresholdMm = thresholdMm;
  }

  public int getThresholdMm() {
    return thresholdMm;
  }

  public ScanChange compare(Stl19pSensor.Scan previous, Stl19pSensor.Scan current) {
    Sector[] before = sectors(previous);
    Sector[] after = sectors(current);
    List<BinChange> differences = new ArrayList<>();
    for (int angle = 0; angle < BINS; angle++) {
      if (before[angle].count == 0 || after[angle].count == 0) continue;
      int previousDistance = before[angle].averageDistance();
      int currentDistance = after[angle].averageDistance();
      if (Math.abs(currentDistance - previousDistance) >= thresholdMm) {
        differences.add(new BinChange(angle, previousDistance, currentDistance, after[angle].averageIntensity()));
      }
    }
    return new ScanChange(previous.getTimestamp(), current.getTimestamp(), differences);
  }

  private static Sector[] sectors(Stl19pSensor.Scan scan) {
    Sector[] sectors = new Sector[BINS];
    for (int i = 0; i < BINS; i++) sectors[i] = new Sector();
    for (Stl19pSensor.Point point : scan.getPoints()) {
      if (point.getDistanceMm() == 0) continue; // No return is not an obstacle.
      int angle = (int) Math.floor(point.getAngleDegrees()) % BINS;
      if (angle < 0) angle += BINS;
      Sector sector = sectors[angle];
      sector.distanceTotal += point.getDistanceMm();
      sector.intensityTotal += point.getIntensity();
      sector.count++;
    }
    return sectors;
  }

  private static final class Sector {
    private long distanceTotal;
    private long intensityTotal;
    private int count;

    private int averageDistance() { return (int) Math.round((double) distanceTotal / count); }
    private int averageIntensity() { return (int) Math.round((double) intensityTotal / count); }
  }

  public record BinChange(int angleDegrees, int previousDistanceMm, int currentDistanceMm, int intensity) {}
  public record ScanChange(String previousTimestamp, String currentTimestamp, List<BinChange> changes) {
    public ScanChange {
      changes = List.copyOf(changes);
    }

    public boolean hasChanges() {
      return !changes.isEmpty();
    }
  }
}
