/*
 * Copyright [ 2024 - 2026 ] MapsMessaging B.V.
 * Licensed under the Apache License, Version 2.0 with the Commons Clause.
 */
package io.mapsmessaging.devices.i2c;

import io.mapsmessaging.devices.impl.AddressableDevice;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/** Logical child: the parent owns all hardware access and the I2C connection. */
public abstract class ComputedI2CDeviceController extends I2CDeviceController {
  private final AtomicBoolean computedClosed = new AtomicBoolean();

  protected ComputedI2CDeviceController(AddressableDevice parentDevice) {
    super(parentDevice);
  }

  public final boolean isComputedClosed() {
    return computedClosed.get();
  }

  protected final void requireComputedOpen() throws IOException {
    if (computedClosed.get()) throw new IOException("Computed I2C device is closed: " + getName());
  }

  /** Read and consume only this endpoint's FIFO; never perform physical device I/O. */
  protected abstract byte[] readComputedState() throws IOException;

  @Override
  public final byte[] getDeviceState() throws IOException {
    requireComputedOpen();
    return readComputedState();
  }

  /** A secondary never inherits register-based configuration from the parent. */
  @Override
  public byte[] getConfigEventDescription() throws IOException {
    JsonObject description = new JsonObject();
    description.addProperty("device", getName());
    description.addProperty("description", getDescription());
    description.add("commands", new JsonObject());
    description.addProperty("readOnly", true);
    return convert(description);
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

  /** Never close the parent's physical sensor or bus. */
  @Override
  public final void close() {
    computedClosed.set(true);
  }
}
