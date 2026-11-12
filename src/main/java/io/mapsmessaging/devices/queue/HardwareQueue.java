/*
 * Copyright (c) 2026 MapsMessaging B.V.
 * Licensed under the project's license.
 */
package io.mapsmessaging.devices.queue;

/** Non-blocking, destructive hardware-style FIFO read interface. */
public interface HardwareQueue<T> {
  /** Remove the oldest complete dataset, or return null if empty. */
  T getData();

  /** Number of complete unread datasets. */
  int queueDepth();

  /** True when at least one unread dataset is present. */
  boolean hasData();

  /** Read and clear the latched overflow indicator. */
  boolean hasOverrun();
}
