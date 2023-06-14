package io.mapsmessaging.server.i2c.devices.sensors;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import io.mapsmessaging.server.i2c.I2CDeviceEntry;
import org.json.JSONObject;

import java.io.IOException;

public class AM2315Manager implements I2CDeviceEntry {

  private final int i2cAddr = 0x5C;
  private final AM2315Sensor sensor;

  public AM2315Manager() {
    sensor = null;
  }

  protected AM2315Manager(I2C device) throws IOException {
    sensor = new AM2315Sensor(device);
  }


  public I2CDeviceEntry mount(I2C device) throws IOException {
    return new AM2315Manager(device);
  }

  public byte[] getPayload() {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("Temperature", sensor.getTemperature());
    jsonObject.put("Humidity", sensor.getHumidity());
    jsonObject.put("Model", sensor.getModel());
    jsonObject.put("Status", sensor.getStatus());
    jsonObject.put("Version", sensor.getVersion());
    return jsonObject.toString(2).getBytes();
  }

  @Override
  public void setPayload(byte[] val) {

  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig();
    config.setComments("i2c device AM2315 encased Temperature and Humidity Sensor https://www.adafruit.com/product/1293");
    config.setSource("I2C bus address : 0x5C");
    config.setVersion("1.0");
    config.setResourceType("sensor");
    config.setInterfaceDescription("Returns JSON object containing Temperature, Humidity, Model, Status and Version");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    return new int[]{i2cAddr};
  }
}
