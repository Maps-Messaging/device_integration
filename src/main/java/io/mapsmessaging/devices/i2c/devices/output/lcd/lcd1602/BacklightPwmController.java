package io.mapsmessaging.devices.i2c.devices.output.lcd.lcd1602;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.I2CDeviceScheduler;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import lombok.Getter;
import org.json.JSONObject;

import java.io.IOException;

public class BacklightPwmController  extends I2CDeviceController {

  private final BacklightPwm pwmController;

  @Getter
  private final String name = "PwmController";
  @Getter
  private final String description = "LCD1602 16*2 lcd display";

  // Used during ServiceLoading
  public BacklightPwmController() {
    pwmController = null;
  }

  protected BacklightPwmController(AddressableDevice device) throws IOException {
    super(device);
    synchronized (I2CDeviceScheduler.getI2cBusLock()) {
      pwmController = new BacklightPwm(device);
    }
  }

  public I2CDevice getDevice() {
    return pwmController;
  }

  @Override
  public boolean detect(AddressableDevice i2cDevice) {
    return pwmController != null && pwmController.isConnected();
  }

  public I2CDeviceController mount(AddressableDevice device) throws IOException {
    return new BacklightPwmController(device);
  }

  public byte[] getDeviceConfiguration() throws IOException {
    JSONObject jsonObject = new JSONObject();
    if (pwmController != null) {
    }
    return jsonObject.toString(2).getBytes();
  }

  public byte[] getDeviceState() throws IOException {
    JSONObject jsonObject = new JSONObject();
    if (pwmController != null) {
    }
    return jsonObject.toString(2).getBytes();
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig();
    config.setComments("i2c device AM2315 encased Temperature and Humidity Sensor https://www.adafruit.com/product/1293");
    config.setSource("I2C bus address : 0x5C");
    config.setVersion("1.0");
    config.setResourceType("sensor");
    config.setInterfaceDescription("temperature, humidity");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    return new int[]{0x60};
  }
}
