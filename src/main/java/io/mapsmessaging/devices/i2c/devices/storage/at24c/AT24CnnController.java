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
import java.util.Base64;

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
  public boolean detect(I2C i2cDevice) {
    return sensor != null && sensor.isConnected();
  }

  public I2CDeviceController mount(I2C device) throws IOException {
    return new AT24CnnController(device);
  }

  public byte[] getStaticPayload() throws IOException {
    JSONObject jsonObject = new JSONObject();
    if (sensor != null) {
      jsonObject.put("size", sensor.getMemorySize());
    }
    return jsonObject.toString(2).getBytes();
  }

  public byte[] getUpdatePayload() throws IOException {
    JSONObject jsonObject = new JSONObject();
    if (sensor != null) {
      jsonObject.put("size", sensor.getMemorySize());
    }
    return jsonObject.toString(2).getBytes();
  }

  public byte[] setPayload(byte[] val) throws IOException {
    JSONObject response = new JSONObject();
    JSONObject jsonObject = new JSONObject(new String(val));
    int address = -1;
    int size = 0;
    byte[] data = null;
    if (jsonObject.has("address")){
      address = jsonObject.getInt("address");
    }
    if(jsonObject.has("size")) {
      size = jsonObject.getInt("size");
    }
    if(jsonObject.has("data")){
      data = Base64.getDecoder().decode(jsonObject.getString("data"));
    }
    if (sensor != null && address != -1) {
      if (data != null) {
        if(data.length + address > sensor.getMemorySize()){
          response.put("status", "Exceeds memory size " + sensor.getMemorySize() + " bytes");
        }
        else {
          sensor.writeBytes(address, data);
          response.put("status", "wrote " + data.length + " bytes");
        }
      } else if (size > 0) {
        size = Math.min(size, sensor.getMemorySize());
        byte[] buff = sensor.readBytes(address, size);
        response.put("data", Base64.getEncoder().encodeToString(buff));
        response.put("status", "read " + buff.length + " bytes");
      }
    } else {
      response.put("status", "Invalid arguments, require address, data or size");
    }
    return response.toString(2).getBytes();
  }


  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig(buildSchema());
    config.setComments("i2c device AT24C32/64 eeprom");
    config.setSource("I2C bus address : 0x57");
    config.setVersion("1.0");
    config.setResourceType("storage");
    config.setInterfaceDescription("Serial EEPROM");
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