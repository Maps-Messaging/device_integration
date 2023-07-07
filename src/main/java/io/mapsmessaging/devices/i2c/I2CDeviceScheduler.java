package io.mapsmessaging.devices.i2c;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.schemas.config.SchemaConfig;

import java.io.IOException;
import java.util.concurrent.Semaphore;

public class I2CDeviceScheduler implements I2CDeviceController {

  private static final Semaphore I2C_BUS_SEMAPHORE = new Semaphore(1);

  private final I2CDeviceController deviceController;

  public I2CDeviceScheduler(I2CDeviceController deviceController){
    this.deviceController = deviceController;
  }

  @Override
  public String getName() {
    return deviceController.getName();
  }

  @Override
  public SchemaConfig getSchema() {
    return deviceController.getSchema();
  }

  @Override
  public byte[] getStaticPayload() {
    try {
      I2C_BUS_SEMAPHORE.acquireUninterruptibly();
      return deviceController.getStaticPayload();
    }
    finally {
      I2C_BUS_SEMAPHORE.release();
    }
  }

  @Override
  public byte[] getUpdatePayload() {
    try {
      I2C_BUS_SEMAPHORE.acquireUninterruptibly();
      return deviceController.getUpdatePayload();
    }
    finally {
      I2C_BUS_SEMAPHORE.release();
    }
  }

  @Override
  public void setPayload(byte[] val) {
    try {
      I2C_BUS_SEMAPHORE.acquireUninterruptibly();
      deviceController.setPayload(val);
    }
    finally {
      I2C_BUS_SEMAPHORE.release();
    }
  }

  @Override
  public I2CDeviceController mount(I2C device) throws IOException {
    throw new IOException("Device already mounted");
  }

  @Override
  public int[] getAddressRange() {
    return deviceController.getAddressRange();
  }

  @Override
  public boolean detect() {
    return true; // This is indeed a physical device
  }
}
