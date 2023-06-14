package io.mapsmessaging.server.i2c.devices.sensors;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import io.mapsmessaging.server.i2c.I2CDeviceEntry;
import java.io.IOException;
import org.json.JSONObject;

public class BMP280Manager implements I2CDeviceEntry {

  private final int i2cAddr = 0x76;
  private BMP280Sensor sensor;

  public BMP280Manager(){
    sensor = null;
  }

  protected BMP280Manager(I2C device) throws IOException {
    sensor = new BMP280Sensor(device);
  }


  public I2CDeviceEntry mount(I2C device) throws IOException {
    return new BMP280Manager(device);
  }
  @Override
  public void setPayload(byte[] val) {

  }

  public byte[] getPayload(){
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("Pressure", sensor.getPressure());
    jsonObject.put("Temperature", sensor.getTemperature());
    return jsonObject.toString(2).getBytes();
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
