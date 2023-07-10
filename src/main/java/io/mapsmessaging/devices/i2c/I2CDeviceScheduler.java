package io.mapsmessaging.devices.i2c;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.schemas.config.SchemaConfig;

import java.io.IOException;

/**
 * The locking here basically disables multiple access to the same device and will limit the
 * access to the I2C bus. If a device calls delay, then another device can take ownership of the bus
 * and perform any operation required. The device that has called delay will need to wait for the
 * new operation on the I2C bus to complete.
 */
public class I2CDeviceScheduler implements I2CDeviceController {

  private static final Object I2C_BUS_LOCK = new Object();

  public static Object getI2cBusLock() {
    return I2C_BUS_LOCK;
  }

  private final I2CDeviceController deviceController;

  public I2CDeviceScheduler(I2CDeviceController deviceController) {
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
    synchronized (deviceController) {
      synchronized (I2C_BUS_LOCK) {
        return deviceController.getStaticPayload();
      }
    }
  }

  @Override
  public byte[] getUpdatePayload() {
    synchronized (deviceController) {
      synchronized (I2C_BUS_LOCK) {
        return deviceController.getUpdatePayload();
      }
    }
  }

  @Override
  public void setPayload(byte[] val) {
    synchronized (deviceController) {
      synchronized (I2C_BUS_LOCK) {
        deviceController.setPayload(val);
      }
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