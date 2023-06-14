package io.mapsmessaging.server.i2c.devices.sensors;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import io.mapsmessaging.server.i2c.I2CDeviceEntry;
import java.io.IOException;
import org.json.JSONObject;

public class TLS2561Manager  implements I2CDeviceEntry {

  private final int i2cAddr = 0x39;
  private TLS2561Sensor sensor;

  public TLS2561Manager(){
    sensor = null;
  }

  protected TLS2561Manager(I2C device) throws IOException {
    sensor = new TLS2561Sensor(device);
  }


  public I2CDeviceEntry mount(I2C device) throws IOException {
    return new TLS2561Manager(device);
  }

  public byte[] getPayload(){
    int[] result = sensor.getLevels();
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("Light", result[0]);
    jsonObject.put("IR", result[1]);
    return jsonObject.toString(2).getBytes();
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig();
    config.setComments("i2c device TLS2561 light sensor, returns light and IR light levels");
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
