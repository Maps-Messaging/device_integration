package io.mapsmessaging.server.i2c.devices.drivers;

import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import io.mapsmessaging.server.i2c.I2CDeviceEntry;
import java.io.IOException;
import org.json.JSONObject;

public class PCA0685Manager implements I2CDeviceEntry {

  private final int i2cAddr = 0x40;
  private PCA9685 sensor;

  public PCA0685Manager() {
    sensor = null;
  }

  public PCA0685Manager(int i2cBusId, int i2cBusAddr) throws IOException {
    try {
      sensor = new PCA9685(i2cBusId, i2cBusAddr);
    } catch (UnsupportedBusNumberException e) {
      throw new IOException(e);
    }
  }


  public I2CDeviceEntry mount(int i2cBusId, int i2cBusAddr) throws IOException {
    if (i2cBusAddr == i2cAddr) {
      return new PCA0685Manager(i2cBusId, i2cBusAddr);
    }
    return null;
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
    config.setInterfaceDescription("Returns JSON object containing Temperature, Humidity, Model, Status and Version");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    return new int[]{i2cAddr};
  }
}