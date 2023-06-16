package io.mapsmessaging.devices.i2c.devices.sensors;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.i2c.I2CDeviceEntry;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import org.json.JSONObject;

import java.io.IOException;

public class AS3935Manager implements I2CDeviceEntry {

  private final int i2cAddr = 0x5C;
  private final AS3935Sensor sensor;

  public AS3935Manager() {
    sensor = null;
  }

  protected AS3935Manager(I2C device) throws IOException {
    sensor = new AS3935Sensor(device, 0, -1);
  }

  @Override
  public boolean detect() {
    return sensor != null && sensor.isConnected();
  }

  public I2CDeviceEntry mount(I2C device) throws IOException {
    return new AM2315Manager(device);
  }

  public byte[] getStaticPayload() {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("Reason", sensor.getReason());
    jsonObject.put("Distance", sensor.getDistance());
    jsonObject.put("MinimumStrikes", sensor.getMinimumStrikes());
    jsonObject.put("Strength", sensor.getStrength());
    jsonObject.put("Registers", sensor.getRegisters());
    return jsonObject.toString(2).getBytes();
  }

  public byte[] getUpdatePayload() {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("Reason", sensor.getReason());
    jsonObject.put("Distance", sensor.getDistance());
    jsonObject.put("MinimumStrikes", sensor.getMinimumStrikes());
    jsonObject.put("Strength", sensor.getStrength());
    jsonObject.put("Registers", sensor.getRegisters());
    return jsonObject.toString(2).getBytes();
  }

  @Override
  public void setPayload(byte[] val) {

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
    return new int[]{i2cAddr};
  }
}
