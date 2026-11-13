/*
 * Copyright [ 2024 - 2026 ] MapsMessaging B.V.
 * Licensed under the Apache License, Version 2.0 with the Commons Clause.
 */
package io.mapsmessaging.devices.serial;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/** Logical child: it must not open, read, or close the parent's physical serial port. */
public abstract class ComputedSerialDeviceController extends SerialDeviceController {
  private final AtomicBoolean computedClosed = new AtomicBoolean();

  public final boolean isComputedClosed() {
    return computedClosed.get();
  }

  protected final void requireComputedOpen() throws IOException {
    if (computedClosed.get()) throw new IOException("Computed serial device is closed: " + getName());
  }

  /** Read and consume only this endpoint's FIFO; never perform physical device I/O. */
  protected abstract byte[] readComputedState() throws IOException;

  @Override
  public final byte[] getDeviceState() throws IOException {
    requireComputedOpen();
    return readComputedState();
  }

  @Override
  public final byte[] getDeviceConfiguration() throws IOException {
    requireComputedOpen();
    return getConfigEventDescription();
  }

  @Override
  public final byte[] updateDeviceConfiguration(byte[] payload) throws IOException {
    requireComputedOpen();
    return updateComputedConfiguration(payload);
  }

  /** Secondary configuration is opt-in and must not modify physical ownership. */
  protected byte[] updateComputedConfiguration(byte[] payload) throws IOException {
    throw new IOException("Computed device is read-only: " + getName());
  }

  @Override
  public final void close() {
    computedClosed.set(true);
  }
}
