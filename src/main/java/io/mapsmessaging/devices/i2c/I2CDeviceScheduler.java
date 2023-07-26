package io.mapsmessaging.devices.i2c;

import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.schemas.config.SchemaConfig;

import java.io.IOException;

/**
 * The locking here basically disables multiple access to the same device and will limit the
 * access to the I2C bus. If a device calls delay, then another device can take ownership of the bus
 * and perform any operation required. The device that has called delay will need to wait for the
 * new operation on the I2C bus to complete.
 */
public class I2CDeviceScheduler extends I2CDeviceController {

  private static final Object I2C_BUS_LOCK = new Object();
  private final I2CDeviceController deviceController;

  public I2CDeviceScheduler(I2CDeviceController deviceController) {
    this.deviceController = deviceController;
  }

  public static Object getI2cBusLock() {
    return I2C_BUS_LOCK;
  }

  @Override
  public int getMountedAddress() {
    return deviceController.getMountedAddress();
  }

  @Override
  public String getName() {
    return deviceController.getName();
  }

  public I2CDevice getDevice() {
    return deviceController.getDevice();
  }

  @Override
  public String getDescription() {
    return deviceController.getDescription();
  }

  @Override
  public SchemaConfig getSchema() {
    return deviceController.getSchema();
  }

  @Override
  public byte[] getDeviceConfiguration() throws IOException {
    synchronized (deviceController) {
      synchronized (I2C_BUS_LOCK) {
        return deviceController.getDeviceConfiguration();
      }
    }
  }

  @Override
  public byte[] getDeviceState() throws IOException {
    synchronized (deviceController) {
      synchronized (I2C_BUS_LOCK) {
        return deviceController.getDeviceState();
      }
    }
  }

  @Override
  public byte[] updateDeviceConfiguration(byte[] val) throws IOException {
    synchronized (deviceController) {
      synchronized (I2C_BUS_LOCK) {
        return deviceController.updateDeviceConfiguration(val);
      }
    }
  }

  @Override
  public I2CDeviceController mount(AddressableDevice device) throws IOException {
    throw new IOException("Device already mounted");
  }

  @Override
  public int[] getAddressRange() {
    return deviceController.getAddressRange();
  }

  @Override
  public boolean detect(AddressableDevice i2cDevice) {
    return true; // This is indeed a physical device
  }
}
