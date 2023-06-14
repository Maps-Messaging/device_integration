package io.mapsmessaging.server.i2c.devices.output.led;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import io.mapsmessaging.server.i2c.I2CDeviceEntry;
import org.json.JSONObject;

public class QuadAlphaNumericManager implements I2CDeviceEntry {

  private final int[] i2cAddr = {0x81};//{0x70, 0x71, 0x72, 0x73, 0x74, 0x75, 0x76, 0x77};
  private final QuadAlphaNumeric display;

  public QuadAlphaNumericManager() {
    display = null;
  }

  public QuadAlphaNumericManager(I2C device) {
    display = new QuadAlphaNumeric(device);
  }


  public I2CDeviceEntry mount(I2C device)  {
    return new QuadAlphaNumericManager(device);
  }

  @Override
  public void setPayload(byte[] val) {

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
    return i2cAddr;
  }
}