package io.mapsmessaging.devices.i2c.devices.sensors;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.i2c.I2CDeviceEntry;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import org.json.JSONObject;

import java.io.IOException;

public class AM2320Manager  implements I2CDeviceEntry {

  private final int i2cAddr = 0x5C;
  private final AM2320Sensor sensor;

  public AM2320Manager() {
    sensor = null;
  }

  protected AM2320Manager(I2C device) throws IOException {
    sensor = new AM2320Sensor(device);
  }


  public I2CDeviceEntry mount(I2C device) throws IOException {
    return new AM2320Manager(device);
  }

  public byte[] getStaticPayload() {
    return "{}".getBytes();
  }


  public byte[] getUpdatePayload() {
    sensor.scanForChange();
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("humidity", sensor.getHumidity());
    jsonObject.put("temperature", sensor.getTemperature());
    return jsonObject.toString(2).getBytes();
  }


  @Override
  public void setPayload(byte[] val) {

  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig();
    config.setComments("i2c device AM2320 Pressure and Temperature Sensor https://learn.adafruit.com/adafruit-am2320-temperature-humidity-i2c-sensor");
    config.setSource("I2C bus address : 0x5C");
    config.setVersion("1.0");
    config.setResourceType("sensor");
    config.setInterfaceDescription("Returns JSON object containing Temperature and Pressure");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    return new int[]{i2cAddr};
  }
}
