package io.mapsmessaging.server.i2c.devices.sensors;

import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import io.mapsmessaging.server.i2c.I2CDeviceEntry;
import java.io.IOException;
import org.json.JSONObject;

public class AS3935Manager implements I2CDeviceEntry {

  private final int i2cAddr = 0x5C;
  private AS3935Sensor sensor;

  public AS3935Manager(){
    sensor = null;
  }

  public AS3935Manager(int i2cBusId, int i2cBusAddr) throws IOException {
    sensor = new AS3935Sensor(i2cBusId, i2cBusAddr, 0, null);
  }


  public I2CDeviceEntry mount(int i2cBusId, int i2cBusAddr) throws IOException {
    if(i2cBusAddr == i2cAddr){
      return new AM2315Manager(i2cBusId, i2cBusAddr);
    }
    return null;
  }

  public byte[] getPayload(){
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("Reason", sensor.getReason());
    jsonObject.put("Distance", sensor.getDistance());
    jsonObject.put("MinimumStrikes", sensor.getMinimumStrikes());
    jsonObject.put("Strength", sensor.getStrength());
    jsonObject.put("Registers", sensor.getRegisters());
    return jsonObject.toString(2).getBytes();
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig();
    config.setComments("i2c device AS3935 is a lightning detector");
    config.setSource("I2C bus address : 0x01, 0x02, 0x03");
    config.setVersion("1.0");
    config.setResourceType("sensor");
    config.setInterfaceDescription("Returns JSON object containing details about the latest detection");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    return new int[]{1,2,3};
  }
}
