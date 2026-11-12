/*
 * Copyright (c) 2026 MapsMessaging B.V.
 * Licensed under the project's license.
 */
package io.mapsmessaging.devices.queue;

import java.util.Objects;

/**
 * Bounded hardware-style FIFO. The producer submits only completed, immutable datasets.
 * On overflow the oldest unread entry is discarded; the producer never waits for consumers.
 * The single monitor makes publication, removal and overrun status linearizable.
 */
public final class HardwareRingBuffer<T> implements HardwareQueue<T> {
  private final Object[] entries;
  private int head;
  private int count;
  private boolean overrun;

  public HardwareRingBuffer(int capacity) {
    if (capacity <= 0) {
      throw new IllegalArgumentException("FIFO capacity must be positive");
    }
    entries = new Object[capacity];
  }

  public synchronized void offer(T dataset) {
    Objects.requireNonNull(dataset, "dataset");
    if (count == entries.length) {
      entries[head] = null;
      head = (head + 1) % entries.length;
      count--;
      overrun = true;
    }
    entries[(head + count) % entries.length] = dataset;
    count++;
  }

  @Override
  @SuppressWarnings("unchecked")
  public synchronized T getData() {
    if (count == 0) {
      return null;
    }
    T result = (T) entries[head];
    entries[head] = null;
    head = (head + 1) % entries.length;
    count--;
    return result;
  }

  @Override
  public synchronized int queueDepth() {
    return count;
  }

  @Override
  public synchronized boolean hasData() {
    return count != 0;
  }

  @Override
  public synchronized boolean hasOverrun() {
    boolean result = overrun;
    overrun = false;
    return result;
  }
}
