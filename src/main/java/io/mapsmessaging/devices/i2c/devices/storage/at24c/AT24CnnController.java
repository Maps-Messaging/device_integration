package io.mapsmessaging.devices.i2c.devices.storage.at24c;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.NamingConstants;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import lombok.Getter;
import org.everit.json.schema.BooleanSchema;
import org.everit.json.schema.NumberSchema;
import org.everit.json.schema.ObjectSchema;
import org.everit.json.schema.StringSchema;
import org.json.JSONObject;

import java.io.IOException;

public class AT24CnnController  extends I2CDeviceController {

  private final AT24CnnDevice sensor;

  @Getter
  private final String name = "AT24C32/64";

  // Used during ServiceLoading
  public AT24CnnController() {
    sensor = null;
  }

  protected AT24CnnController(I2C device) throws IOException {
    super(device);
    sensor = new AT24CnnDevice(device);
  }

  @Override
  public boolean detect() {
    return sensor != null && sensor.isConnected();
  }

  public I2CDeviceController mount(I2C device) throws IOException {
    return new AT24CnnController(device);
  }

  public byte[] getStaticPayload() throws IOException {
    JSONObject jsonObject = new JSONObject();
    if (sensor != null) {

    }
    return jsonObject.toString(2).getBytes();
  }

  public byte[] getUpdatePayload() throws IOException {
    JSONObject jsonObject = new JSONObject();
    if (sensor != null) {
      byte[] data = sensor.readBytes(0, 32*1024/8);
      jsonObject.put("data", sensor.dump(data, data.length));
      for(int x=0;x<data.length;x++){
        data[x] = (byte) (~data[x]);
      }
      sensor.writeBytes(0, data);
    }
    return jsonObject.toString(2).getBytes();
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig(buildSchema());
    config.setComments("i2c device AT24C32/64 eeprom");
    config.setSource("I2C bus address : 0x57");
    config.setVersion("1.0");
    config.setResourceType("sensor");
    config.setInterfaceDescription("temperature, humidity");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    int i2cAddr = 0x57;
    return new int[]{i2cAddr};
  }


  private String buildSchema() {
    ObjectSchema.Builder staticSchema = ObjectSchema.builder()
        .addPropertySchema("model",
            StringSchema.builder()
                .description("Model number of sensor")
                .build()
        )
        .addPropertySchema("id",
            StringSchema.builder()
                .description("Unique ID of sensor")
                .build()
        )
        .addPropertySchema("status",
            BooleanSchema.builder()
                .description("Current Status")
                .build())
        .addPropertySchema("version",
            BooleanSchema.builder()
                .description("Chip Version")
                .build());

    ObjectSchema.Builder updateSchema = ObjectSchema.builder()
        .addPropertySchema("temperature",
            NumberSchema.builder()
                .minimum(-40.0)
                .maximum(80.0)
                .description("Temperature")
                .build()
        )
        .addPropertySchema("humidity",
            NumberSchema.builder()
                .minimum(0.0)
                .maximum(100.0)
                .description("Humidity")
                .build()
        );

    ObjectSchema.Builder schemaBuilder = ObjectSchema.builder();
    schemaBuilder
        .addPropertySchema(NamingConstants.SENSOR_DATA_SCHEMA, updateSchema.build())
        .addPropertySchema(NamingConstants.DEVICE_STATIC_DATA_SCHEMA, staticSchema.build())
        .description("Humidity and Temperature Module")
        .title("AM2315");

    return schemaToString(schemaBuilder.build());
  }
}