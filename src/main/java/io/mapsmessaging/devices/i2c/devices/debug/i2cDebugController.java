package io.mapsmessaging.devices.i2c.devices.debug;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.I2CDeviceScheduler;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;

import java.io.IOException;

public class i2cDebugController extends I2CDeviceController {

  private static final int I2C_ADDRESS = 0x0;
  private static final String NAME = "DEBUG";
  private static final String DESCRIPTION = "i2c Debug Device, simply updates the time every second";

  private final i2cDebugDevice device;

  public i2cDebugController() {
    device = null;
  }

  public i2cDebugController(AddressableDevice device) {
    super(device);
    synchronized (I2CDeviceScheduler.getI2cBusLock()) {
      this.device = new i2cDebugDevice(device);
    }
  }

  public I2CDevice getDevice() {
    return device;
  }

  public DeviceType getType(){
    return device.getType();
  }

  public I2CDeviceController mount(AddressableDevice device) throws IOException {
    return new i2cDebugController(device);
  }

  @Override
  public boolean detect(AddressableDevice i2cDevice) {
    return device != null && device.isConnected();
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String getDescription() {
    return DESCRIPTION;
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig();
    config.setComments(DESCRIPTION);
    config.setSource(getName());
    config.setVersion("1.0");
    config.setResourceType("sensor");
    config.setInterfaceDescription("Debug device, updates time");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    return new int[]{I2C_ADDRESS};
  }
}