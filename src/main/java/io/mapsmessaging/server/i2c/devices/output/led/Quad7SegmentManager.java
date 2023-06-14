package io.mapsmessaging.server.i2c.devices.output.led;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import io.mapsmessaging.server.i2c.I2CDeviceEntry;
import org.json.JSONObject;

public class Quad7SegmentManager implements I2CDeviceEntry {

  private final int[] i2cAddr = {0x70, 0x71, 0x72, 0x73, 0x74, 0x75, 0x76, 0x77};
  private final Quad7Segment display;

  public Quad7SegmentManager() {
    display = null;
  }

  public Quad7SegmentManager(I2C device) {
    display = new Quad7Segment(device);
    display.turnOn();
  }



  public I2CDeviceEntry mount(I2C device)  {
    return new Quad7SegmentManager(device);
  }

  @Override
  public void setPayload(byte[] val) {
    display.setBrightness((byte)0x8);
    display.write(new String(val));
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