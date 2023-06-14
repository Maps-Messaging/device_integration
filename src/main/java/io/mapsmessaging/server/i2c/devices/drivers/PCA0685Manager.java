package io.mapsmessaging.server.i2c.devices.drivers;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import io.mapsmessaging.server.i2c.I2CDeviceEntry;
import java.io.IOException;
import org.json.JSONObject;

public class PCA0685Manager implements I2CDeviceEntry {

  private final int i2cAddr = 0x40;
  private final PCA9685Device sensor;

  public PCA0685Manager() {
    sensor = null;
  }

  public PCA0685Manager(I2C device) throws IOException {
    sensor = new PCA9685Device(device);
  }


  public I2CDeviceEntry mount(I2C device) throws IOException {
    return new PCA0685Manager(device);
  }

  public byte[] getPayload() {
    JSONObject jsonObject = new JSONObject();
    return jsonObject.toString(2).getBytes();
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