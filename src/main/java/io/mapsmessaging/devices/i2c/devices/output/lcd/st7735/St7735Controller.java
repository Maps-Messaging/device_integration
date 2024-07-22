package io.mapsmessaging.devices.i2c.devices.output.lcd.st7735;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.I2CDeviceScheduler;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.devices.util.UuidGenerator;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import org.json.JSONObject;

import java.io.IOException;

public class St7735Controller extends I2CDeviceController {

  private static final String NAME = "ST7735";
  private static final String DESCRIPTION = "ST7735 lcd display";

  private final St7735Device display;

  // Used during ServiceLoading
  public St7735Controller() {
    display = null;
  }

  protected St7735Controller(AddressableDevice device) throws IOException {
    super(device);
    synchronized (I2CDeviceScheduler.getI2cBusLock()) {
      display = new St7735Device(device);
    }
  }

  public I2CDevice getDevice() {
    return display;
  }

  public DeviceType getType(){
    return getDevice().getType();
  }


  @Override
  public boolean detect(AddressableDevice i2cDevice) {
    return display != null && display.isConnected();
  }

  public I2CDeviceController mount(AddressableDevice device) throws IOException {
    return new St7735Controller(device);
  }

  @Override
  public byte[] updateDeviceConfiguration(byte[] val) throws IOException {
    return new byte[0];
  }

  @Override
  public byte[] getDeviceConfiguration() throws IOException {
    JSONObject jsonObject = new JSONObject();
    if (display != null) {
      //
    }
    return jsonObject.toString(2).getBytes();
  }

  @Override
  public byte[] getDeviceState() throws IOException {
    JSONObject jsonObject = new JSONObject();
    if (display != null) {
      //
    }
    return jsonObject.toString(2).getBytes();
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
    config.setUniqueId(UuidGenerator.getInstance().generateUuid(getName()));
    config.setResourceType("display");
    config.setInterfaceDescription("display");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    return new int[]{0x18};
  }
}
