package io.mapsmessaging.devices.i2c.devices.sensors;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import io.mapsmessaging.devices.i2c.I2CDeviceEntry;
import lombok.Getter;
import org.json.JSONObject;

import java.io.IOException;

public class TLS2561Manager implements I2CDeviceEntry {

  private final int i2cAddr = 0x39;
  private final TLS2561Sensor sensor;

  @Getter
  private final String name = "TLS2561";


  public TLS2561Manager() {
    sensor = null;
  }

  public TLS2561Manager(I2C device) throws IOException {
    sensor = new TLS2561Sensor(device);
  }

  @Override
  public boolean detect() {
    return sensor != null && sensor.isConnected();
  }

  public I2CDeviceEntry mount(I2C device) throws IOException {
    return new TLS2561Manager(device);
  }

  public byte[] getStaticPayload() {
    return "{}".getBytes();
  }

  public byte[] getUpdatePayload() {
    int[] result = sensor.getLevels();
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("ch0", result[0]);
    jsonObject.put("ch1", result[1]);
    jsonObject.put("lux", sensor.calculateLux());
    return jsonObject.toString(2).getBytes();
  }

  @Override
  public void setPayload(byte[] val) {}

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig();
    config.setComments("i2c device TLS2561 light sensor, returns light and IR light levels and computed lux level");
    config.setSource("I2C bus address : 0x39");
    config.setVersion("1.0");
    config.setResourceType("sensor");
    config.setInterfaceDescription("Returns JSON object containing light and IR light levels");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    return new int[]{i2cAddr};
  }
}
