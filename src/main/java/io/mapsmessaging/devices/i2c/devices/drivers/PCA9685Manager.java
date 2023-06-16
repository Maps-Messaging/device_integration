package io.mapsmessaging.devices.i2c.devices.drivers;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.i2c.I2CDeviceEntry;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;

import java.io.IOException;
import org.json.JSONObject;

public class PCA9685Manager implements I2CDeviceEntry {

  private final int i2cAddr = 0x40;
  private final PCA9685Device sensor;

  public PCA9685Manager() {
    sensor = null;
  }

  public PCA9685Manager(I2C device) throws IOException {
    sensor = new PCA9685Device(device);
  }


  public I2CDeviceEntry mount(I2C device) throws IOException {
    return new PCA9685Manager(device);
  }

  public byte[] getStaticPayload() {
    JSONObject jsonObject = new JSONObject();
    return jsonObject.toString(2).getBytes();
  }

  public byte[] getUpdatePayload() {
    JSONObject jsonObject = new JSONObject();
    return jsonObject.toString(2).getBytes();
  }

  @Override
  public void setPayload(byte[] val) {

  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig();
    config.setComments("i2c device PCA9685 supports 16 PWM devices like servos or LEDs");
    config.setSource("I2C bus address : 0x40");
    config.setVersion("1.0");
    config.setResourceType("driver");
    config.setInterfaceDescription("Manages the output of 16 PWM devices");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    return new int[]{i2cAddr};
  }
}