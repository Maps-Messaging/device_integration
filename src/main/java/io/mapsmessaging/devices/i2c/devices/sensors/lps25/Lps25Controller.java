package io.mapsmessaging.devices.i2c.devices.sensors.lps25;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.NamingConstants;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.I2CDeviceScheduler;
import io.mapsmessaging.devices.i2c.devices.sensors.lps25.values.DataRate;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import lombok.Getter;
import org.everit.json.schema.ObjectSchema;
import org.json.JSONObject;

import java.io.IOException;

public class Lps25Controller extends I2CDeviceController {

  private final Lps25Sensor sensor;

  @Getter
  private final String name = "LPS25";
  @Getter
  private final String description = "Pressure and Temperature sensor";


  public Lps25Controller() {
    sensor = null;
  }

  public Lps25Controller(I2C device) throws IOException {
    super(device);
    sensor = new Lps25Sensor(device);
    synchronized (I2CDeviceScheduler.getI2cBusLock()) {
      sensor.softReset();
      sensor.setPowerDownMode(true);
      sensor.setDataRate(DataRate.RATE_1_HZ);
    }
  }
  
  public I2CDevice getDevice(){
    return sensor;
  }

  @Override
  public boolean canDetect() {
    return true;
  }

  @Override
  public boolean detect(I2C i2cDevice) {
    return (Lps25Sensor.getId(i2cDevice) == 0b10111101);
  }

  public I2CDeviceController mount(I2C device) throws IOException {
    return new Lps25Controller(device);
  }


  @Override
  public byte[] setPayload(byte[] val) throws IOException {
    if (sensor != null) {
      return JsonHelper.unpackJson(new JSONObject(new String(val)), sensor);
    }
    return ("{}").getBytes();
  }


  public byte[] getStaticPayload() throws IOException {
    JSONObject jsonObject = new JSONObject();
    if (sensor != null) {
      return JsonHelper.packStaticPayload(sensor).toString(2).getBytes();
    }
    return jsonObject.toString(2).getBytes();
  }

  public byte[] getUpdatePayload() throws IOException {
    JSONObject jsonObject = new JSONObject();
    if (sensor != null) {
      jsonObject.put("temperature", round(sensor.getTemperature(), 1));
      jsonObject.put("pressure", round(sensor.getPressure(),2));
    }
    return jsonObject.toString(2).getBytes();
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig(buildSchema());
    config.setComments("i2c device LPS25 pressure sensor: 260-1260 hPa");
    config.setSource("I2C bus address : 0x5d");
    config.setVersion("1.0");
    config.setResourceType("sensor");
    config.setInterfaceDescription("Returns JSON object containing pressure and temperature");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    int i2cAddr = 0x5D;
    return new int[]{i2cAddr};
  }

  private String buildSchema() {
    ObjectSchema.Builder schemaBuilder = ObjectSchema.builder();
    schemaBuilder
        // .addPropertySchema("staticPayloadSchema", staticPayloadSchema.build())
        // .addPropertySchema("updatePayloadSchema", updatePayloadSchema.build())
        .addPropertySchema(NamingConstants.DEVICE_WRITE_SCHEMA, JsonHelper.generateSchema().build())
        .description("Pressure and Temperature sensor")
        .id("LPS25");

    return schemaToString(schemaBuilder.build());
  }
}
