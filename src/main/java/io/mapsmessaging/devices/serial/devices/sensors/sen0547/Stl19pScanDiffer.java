/*
 * Copyright [ 2024 - 2026 ] MapsMessaging B.V.
 * Licensed under the Apache License, Version 2.0 with the Commons Clause.
 */
package io.mapsmessaging.devices.serial.devices.sensors.sen0547;

import java.util.ArrayList;
import java.util.List;

/** Compares completed scans, never partial serial frames. */
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
    boolean merged = current.isMerged();
    int[] before = bins(previous, merged);
    int[] after = bins(current, merged);
    List<BinChange> differences = new ArrayList<>();
    int limit = thresholdMm;
    for (int angle = 0; angle < before.length; angle++) {
      if (before[angle] > 0 && after[angle] > 0
          && Math.abs(after[angle] - before[angle]) >= limit) {
        differences.add(new BinChange(angle, before[angle], after[angle]));
      }
    }
    return new ScanChange(previous.getTimestamp(), current.getTimestamp(), differences);
  }

  private static int[] bins(Stl19pSensor.Scan scan, boolean merged) {
    int size = merged ? 3600 : BINS;
    int[] distances = new int[size];
    int[] counts = new int[size];
    for (Stl19pSensor.Point point : scan.getPoints()) {
      int distance = point.getDistanceMm();
      if (distance == 0) continue; // Invalid/no return; never interpret as an obstacle.
      int angle = merged ? (int) Math.round(point.getAngleDegrees() * 10.0) % size
          : (int) Math.floor(point.getAngleDegrees()) % size;
      if (angle < 0) angle += size;
      distances[angle] += distance;
      counts[angle]++;
    }
    for (int i = 0; i < size; i++) {
      if (counts[i] > 0) distances[i] /= counts[i];
    }
    return distances;
  }

  public record BinChange(int angleDegrees, int previousDistanceMm, int currentDistanceMm) {}
  public record ScanChange(String previousTimestamp, String currentTimestamp, List<BinChange> changes) {
    public ScanChange {
      changes = List.copyOf(changes);
    }
    public boolean hasChanges() {
      return !changes.isEmpty();
    }
  }
}
