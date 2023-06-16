package io.mapsmessaging.devices.i2c.devices.sensors;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.i2c.I2CDeviceEntry;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import org.json.JSONObject;

import java.io.IOException;

public class BMP280Manager implements I2CDeviceEntry {

  private final int i2cAddr = 0x76;
  private final BMP280Sensor sensor;

  public BMP280Manager() {
    sensor = null;
  }

  protected BMP280Manager(I2C device) throws IOException {
    sensor = new BMP280Sensor(device);
  }

  @Override
  public boolean detect() {
    return sensor != null && sensor.isConnected();
  }

  public I2CDeviceEntry mount(I2C device) throws IOException {
    return new BMP280Manager(device);
  }

  public byte[] getStaticPayload() {
    return "{}".getBytes();
  }


  public byte[] getUpdatePayload() {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("pressure", sensor.getPressure());
    jsonObject.put("temperature", sensor.getTemperature());
    return jsonObject.toString(2).getBytes();
  }


  @Override
  public void setPayload(byte[] val) {

  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig();
    config.setComments("i2c device BMP280 Pressure and Temperature Sensor https://www.bosch-sensortec.com/products/environmental-sensors/pressure-sensors/bmp280/");
    config.setSource("I2C bus address : 0x76");
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
