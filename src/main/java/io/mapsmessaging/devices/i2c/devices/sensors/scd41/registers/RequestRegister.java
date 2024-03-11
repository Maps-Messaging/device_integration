package io.mapsmessaging.devices.i2c.devices.sensors.scd41.registers;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.devices.Register;
import io.mapsmessaging.devices.i2c.devices.sensors.scd41.functions.Request;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

public abstract class RequestRegister extends Register {

  private static final AtomicLong counter = new AtomicLong(0);
  protected Request request;

  protected RequestRegister(I2CDevice sensor,String name, Request request) {
    super(sensor, (int)counter.getAndIncrement(), name);
    this.request = request;
  }

  @Override
  protected void reload() throws IOException {
    // No need for this in a request
  }

  @Override
  protected void setControlRegister(int mask, int value) throws IOException {
    // No need for this in a request
  }

  @Override
  public String toString(int maxLength) {
    return "";
  }

}

