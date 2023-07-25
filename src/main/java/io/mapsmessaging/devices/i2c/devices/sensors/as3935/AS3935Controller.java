/*
 *      Copyright [ 2020 - 2023 ] [Matthew Buckton]
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package io.mapsmessaging.devices.i2c.devices.sensors.as3935;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.NamingConstants;
import io.mapsmessaging.devices.deviceinterfaces.AbstractRegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.sensorreadings.ComputationResult;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import lombok.Getter;
import org.everit.json.schema.BooleanSchema;
import org.everit.json.schema.NumberSchema;
import org.everit.json.schema.ObjectSchema;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class AS3935Controller extends I2CDeviceController {

  private final int i2cAddr = 0x03;
  private final AS3935Sensor sensor;

  @Getter
  private final String name = "AS3935";
  @Getter
  private final String description = "Lightning Detector";

  // Used during ServiceLoading
  public AS3935Controller() {
    sensor = null;
  }

  protected AS3935Controller(I2C device) throws IOException {
    super(device);
    sensor = new AS3935Sensor(device, 7);
  }

  public I2CDevice getDevice() {
    return sensor;
  }

  @Override
  public boolean detect(I2C i2cDevice) {
    return sensor != null && sensor.isConnected();
  }

  public I2CDeviceController mount(I2C device) throws IOException {
    return new AS3935Controller(device);
  }

  @Override
  public byte[] updateDeviceConfiguration(byte[] val) throws IOException {
    if (sensor != null) {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, AbstractRegisterData.class);
      List<AbstractRegisterData> data = objectMapper.readValue(new String(val), type);
      sensor.getRegisterMap().setData(data);
    }
    return ("{}").getBytes();
  }

  @Override
  public byte[] getDeviceConfiguration() throws IOException {
    JSONObject jsonObject = new JSONObject();
    if (sensor != null) {
      ObjectMapper objectMapper = new ObjectMapper();
      String json = objectMapper.writeValueAsString(sensor.getRegisterMap().getData());
      return json.getBytes();
    }
    return jsonObject.toString(2).getBytes();
  }

  @Override
  public byte[] getDeviceState() throws IOException {
    JSONObject jsonObject = new JSONObject();
    if (sensor != null) {
      List<SensorReading<?>> readings = sensor.getReadings();
      for (SensorReading<?> reading : readings) {
        ComputationResult<?> computationResult = reading.getValue();
        if (!computationResult.hasError()) {
          jsonObject.put(reading.getName(), computationResult.getResult());
        } else {
          jsonObject.put(reading.getName(), computationResult.getError().getMessage());
        }
      }
    }
    return jsonObject.toString(2).getBytes();
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig(buildSchema());
    config.setComments("i2c device AS3935 is a lightning detector");
    config.setSource("I2C bus address : 0x01, 0x02, 0x03");
    config.setVersion("1.0");
    config.setResourceType("sensor");
    config.setInterfaceDescription("Returns JSON object containing details about the latest detection");
    return config;
  }


  private String buildSchema() {
    ObjectSchema.Builder staticPayloadSchema = ObjectSchema.builder()
        .addPropertySchema("AFE_PowerDown",
            BooleanSchema.builder()
                .description("AFE Power Down status")
                .build()
        )
        .addPropertySchema("AFE_GainBoost",
            NumberSchema.builder()
                .minimum(0)
                .maximum(31) // Adjust as needed
                .description("AFE Gain Boost")
                .build()
        )
        .addPropertySchema("WatchdogThreshold",
            NumberSchema.builder()
                .minimum(0)
                .maximum(7) // Adjust as needed
                .description("Watchdog Threshold")
                .build()
        )
        .addPropertySchema("NoiseFloorLevel",
            NumberSchema.builder()
                .minimum(0)
                .maximum(1070) // Adjust as needed
                .description("Noise Floor Level")
                .build()
        )
        .addPropertySchema("SpikeRejection",
            NumberSchema.builder()
                .minimum(0)
                .maximum(31) // Adjust as needed
                .description("Spike Rejection")
                .build()
        )
        .addPropertySchema("MinNumLightning",
            NumberSchema.builder()
                .minimum(1)
                .maximum(10) // Adjust as needed
                .description("Minimum Number of Lightning")
                .build()
        )
        .addPropertySchema("ClearStatisticsEnabled",
            BooleanSchema.builder()
                .description("Clear Statistics Enabled status")
                .build()
        )
        .addPropertySchema("TuningCap",
            NumberSchema.builder()
                .minimum(0)
                .maximum(120) // Adjust as needed
                .description("Tuning Cap")
                .build()
        )
        .addPropertySchema("DispTRCOEnabled",
            BooleanSchema.builder()
                .description("Disp TRCO Enabled status")
                .build()
        )
        .addPropertySchema("DispSRCOEnabled",
            BooleanSchema.builder()
                .description("Disp SRCO Enabled status")
                .build()
        );
    ObjectSchema.Builder updatePayloadSchema = ObjectSchema.builder()
        .addPropertySchema("energy",
            NumberSchema.builder()
                .description("Energy")
                .build()
        )
        .addPropertySchema("interruptReason",
            NumberSchema.builder()
                .description("Interrupt Reason")
                .build()
        )
        .addPropertySchema("distance",
            NumberSchema.builder()
                .description("Distance Estimation")
                .build()
        );

    ObjectSchema.Builder setPayloadSchema = ObjectSchema.builder()
        // AFE_GAIN Register
        .addPropertySchema("AFE_PowerDown",
            BooleanSchema.builder()
                .description("AFE Power Down status")
                .build()
        )
        .addPropertySchema("AFE_GainBoost",
            NumberSchema.builder()
                .minimum(0)
                .maximum(3)
                .description("AFE Gain Boost")
                .build()
        )
        // THRESHOLD Register
        .addPropertySchema("WatchdogThreshold",
            NumberSchema.builder()
                .minimum(0)
                .maximum(10)
                .description("Watchdog Threshold")
                .build()
        )
        .addPropertySchema("NoiseFloorLevel",
            NumberSchema.builder()
                .minimum(0)
                .maximum(7)
                .description("Noise Floor Level")
                .build()
        )
        // LIGHTNING_REG Register
        .addPropertySchema("SpikeRejection",
            NumberSchema.builder()
                .minimum(0)
                .maximum(3)
                .description("Spike Rejection")
                .build()
        )
        .addPropertySchema("MinNumLightning",
            NumberSchema.builder()
                .minimum(0)
                .maximum(3)
                .description("Minimum Number of Lightning")
                .build()
        )
        .addPropertySchema("ClearStatisticsEnabled",
            BooleanSchema.builder()
                .description("Clear Statistics Enabled status")
                .build()
        )
        // INTERRUPT Register
        .addPropertySchema("MaskDisturberEnabled",
            BooleanSchema.builder()
                .description("Mask Disturber Enabled status")
                .build()
        )
        .addPropertySchema("EnergyDivRatio",
            NumberSchema.builder()
                .minimum(0)
                .maximum(3)
                .description("Energy Div Ratio")
                .build()
        )
        // TUN_CAP Register
        .addPropertySchema("TuningCap",
            NumberSchema.builder()
                .minimum(0)
                .maximum(15)
                .description("Tuning Cap")
                .build()
        )
        .addPropertySchema("DispTRCOEnabled",
            BooleanSchema.builder()
                .description("Disp TRCO Enabled status")
                .build()
        )
        .addPropertySchema("DispSRCOEnabled",
            BooleanSchema.builder()
                .description("Disp SRCO Enabled status")
                .build()
        );

    ObjectSchema.Builder schemaBuilder = ObjectSchema.builder();
    schemaBuilder
        .addPropertySchema(NamingConstants.DEVICE_STATIC_DATA_SCHEMA, staticPayloadSchema.build())
        .addPropertySchema(NamingConstants.DEVICE_WRITE_SCHEMA, updatePayloadSchema.build())
        .addPropertySchema(NamingConstants.SENSOR_DATA_SCHEMA, setPayloadSchema.build())
        .description("Lightning Detector")
        .title("AS3935");

    return schemaToString(schemaBuilder.build());
  }

  @Override
  public int[] getAddressRange() {
    return new int[]{i2cAddr};
  }
}
