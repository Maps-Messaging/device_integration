package io.mapsmessaging.server.i2c.devices.sensors;

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

  public BMP280Manager(int i2cBusId, int i2cBusAddr) throws IOException {
    sensor = new BMP280Sensor(i2cBusId, i2cBusAddr);
  }


  public I2CDeviceEntry mount(int i2cBusId, int i2cBusAddr) throws IOException {
    if(i2cBusAddr == i2cAddr){
      return new BMP280Manager(i2cBusId, i2cBusAddr);
    }
    return null;
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
