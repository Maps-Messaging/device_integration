package io.mapsmessaging.devices.i2c.devices.sensors.lps35;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.i2c.I2CDeviceEntry;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import lombok.Getter;
import org.everit.json.schema.BooleanSchema;
import org.everit.json.schema.NumberSchema;
import org.everit.json.schema.ObjectSchema;
import org.everit.json.schema.StringSchema;
import org.json.JSONObject;

public class Lps35Controller implements I2CDeviceEntry {

  private final Lps35Sensor sensor;

  @Getter
  private final String name = "LPS35";

  private final AltitudeMonitor altitudeMonitor;

  public Lps35Controller() {
    sensor = null;
    altitudeMonitor = new AltitudeMonitor();
  }

  public Lps35Controller(I2C device) {
    sensor = new Lps35Sensor(device);
    sensor.setDataRate(DataRate.RATE_10_HZ);
    altitudeMonitor = new AltitudeMonitor(15.6f, 1009.9f);
  }

  @Override
  public boolean detect() {
    return sensor != null && sensor.isConnected();
  }

  public I2CDeviceEntry mount(I2C device) {
    return new Lps35Controller(device);
  }

  public byte[] getStaticPayload() {
    JSONObject jsonObject = new JSONObject();
    if (sensor != null) {

    }
    return jsonObject.toString(2).getBytes();
  }

  public byte[] getUpdatePayload() {
    JSONObject jsonObject = new JSONObject();
    if(sensor != null){
      float temp = sensor.getTemperature();
      float pres = sensor.getPressure();
      double altitude = altitudeMonitor.compute(pres, temp);
      if(altitude == Double.NaN || altitude == Double.POSITIVE_INFINITY || altitude == Double.NEGATIVE_INFINITY){
        altitude = 0;
      }
      jsonObject.put("temperature", temp);
      jsonObject.put("pressure", pres);
      jsonObject.put("altitudeDiff", altitude);
      jsonObject.put("whoAmI", sensor.whoAmI());

    }
    return jsonObject.toString(2).getBytes();
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig(buildSchema());
    config.setComments("i2c device LPS35 pressure sensor: 260-1260 hPa");
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
    ObjectSchema.Builder staticSchema = ObjectSchema.builder()
        .addPropertySchema("integration",
            StringSchema.builder()
                .pattern("^MS_\\d{1,3}$")
                .description("Integration time to compute the values, 14ms, 101ms and 402ms default 402")
                .build()
        )
        .addPropertySchema("highGain",
            BooleanSchema.builder()
                .description("High Gain enabled or disabled")
                .build());

    ObjectSchema.Builder updateSchema = ObjectSchema.builder()
        .addPropertySchema("ch0",
            NumberSchema.builder()
                .minimum(0)
                .maximum(65535)
                .description("Light and IR levels")
                .build()
        )
        .addPropertySchema("ch1",
            NumberSchema.builder()
                .minimum(0)
                .maximum(65535)
                .description("IR levels")
                .build()
        )
        .addPropertySchema("lux",
            NumberSchema.builder()
                .description("Computed LUX value")
                .build()
        );

    ObjectSchema.Builder schemaBuilder = ObjectSchema.builder();
    schemaBuilder
        .addPropertySchema("updateSchema", updateSchema.build())
        .addPropertySchema("staticSchema", staticSchema.build())
        .description("Digital Luminosity/Lux/Light Sensor Breakout")
        .title("TLS2561");

    ObjectSchema schema = schemaBuilder.build();
    return schema.toString();
  }
}
