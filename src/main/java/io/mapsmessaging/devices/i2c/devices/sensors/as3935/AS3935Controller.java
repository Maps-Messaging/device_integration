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

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.i2c.I2CDeviceEntry;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import lombok.Getter;
import org.everit.json.schema.BooleanSchema;
import org.everit.json.schema.NumberSchema;
import org.everit.json.schema.ObjectSchema;
import org.json.JSONObject;

import java.io.IOException;

public class AS3935Controller implements I2CDeviceEntry {

  private final int i2cAddr = 0x03;
  private final AS3935Sensor sensor;

  @Getter
  private final String name = "AS3935";

  public AS3935Controller() {
    sensor = null;
  }

  protected AS3935Controller(I2C device) throws IOException {
    sensor = new AS3935Sensor(device, 7);
  }

  @Override
  public boolean detect() {
    return sensor != null && sensor.isConnected();
  }

  public I2CDeviceEntry mount(I2C device) throws IOException {
    return new AS3935Controller(device);
  }

  public byte[] getStaticPayload() {
    JSONObject jsonObject = new JSONObject();
    if (sensor != null) {
      JSONObject jsonConfig = new JSONObject();
      Registers registers = sensor.getRegisters();
      // AFE_GAIN Register
      jsonConfig.put("AFE_PowerDown", registers.isAFE_PowerDown());
      jsonConfig.put("AFE_GainBoost", registers.getAFE_GainBoost());

      // THRESHOLD Register
      jsonConfig.put("WatchdogThreshold", registers.getWatchdogThreshold());
      jsonConfig.put("NoiseFloorLevel", registers.getNoiseFloorLevel());

      // LIGHTNING_REG Register
      jsonConfig.put("SpikeRejection", registers.getSpikeRejection());
      jsonConfig.put("MinNumLightning", registers.getMinNumLightning());
      jsonConfig.put("ClearStatisticsEnabled", registers.isClearStatisticsEnabled());

      // TUN_CAP Register
      jsonConfig.put("TuningCap", registers.getTuningCap());
      jsonConfig.put("DispTRCOEnabled", registers.isDispTRCOEnabled());
      jsonConfig.put("DispSRCOEnabled", registers.isDispSRCOEnabled());
    }
    return jsonObject.toString(2).getBytes();
  }

  public byte[] getUpdatePayload() {
    JSONObject jsonObject = new JSONObject();
    if(sensor != null) {
      Registers registers = sensor.getRegisters();
      jsonObject.put("energy", registers.getEnergy());
      jsonObject.put("interruptReason", registers.getInterruptReason());
      jsonObject.put("distance", registers.getDistanceEstimation());
    }
    return jsonObject.toString(2).getBytes();
  }

  @Override
  public void setPayload(byte[] payload){
    JSONObject jsonConfig = new JSONObject(new String(payload));
    Registers registers = sensor.getRegisters();

    // AFE_GAIN Register
    if (jsonConfig.has("AFE_PowerDown")) {
      registers.setAFE_PowerDown(jsonConfig.getBoolean("AFE_PowerDown"));
    }
    if (jsonConfig.has("AFE_GainBoost")) {
      registers.setAFE_GainBoost(jsonConfig.getInt("AFE_GainBoost"));
    }

    // THRESHOLD Register
    if (jsonConfig.has("WatchdogThreshold")) {
      registers.setWatchdogThreshold(jsonConfig.getInt("WatchdogThreshold"));
    }
    if (jsonConfig.has("NoiseFloorLevel")) {
      registers.setNoiseFloorLevel(jsonConfig.getInt("NoiseFloorLevel"));
    }

    // LIGHTNING_REG Register
    if (jsonConfig.has("SpikeRejection")) {
      registers.setSpikeRejection(jsonConfig.getInt("SpikeRejection"));
    }
    if (jsonConfig.has("MinNumLightning")) {
      registers.setMinNumLightning(jsonConfig.getInt("MinNumLightning"));
    }
    if (jsonConfig.has("ClearStatisticsEnabled")) {
      registers.setClearStatisticsEnabled(jsonConfig.getBoolean("ClearStatisticsEnabled"));
    }
    // INTERRUPT Register
    if (jsonConfig.has("MaskDisturberEnabled")) {
      registers.setMaskDisturberEnabled(jsonConfig.getBoolean("MaskDisturberEnabled"));
    }
    if (jsonConfig.has("EnergyDivRatio")) {
      registers.setEnergyDivRatio(jsonConfig.getInt("EnergyDivRatio"));
    }
    // TUN_CAP Register
    if (jsonConfig.has("TuningCap")) {
      registers.setTuningCap(jsonConfig.getInt("TuningCap"));
    }
    if (jsonConfig.has("DispTRCOEnabled")) {
      registers.setDispTRCOEnabled(jsonConfig.getBoolean("DispTRCOEnabled"));
    }
    if (jsonConfig.has("DispSRCOEnabled")) {
      registers.setDispSRCOEnabled(jsonConfig.getBoolean("DispSRCOEnabled"));
    }
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


  private String buildSchema(){
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
        .addPropertySchema("staticPayloadSchema", staticPayloadSchema.build())
        .addPropertySchema("updatePayloadSchema", updatePayloadSchema.build())
        .addPropertySchema("updateSchema", setPayloadSchema.build())
        .description("Lightning Detector")
        .title("AS3935");

    return schemaToString(schemaBuilder.build());
  }
  @Override
  public int[] getAddressRange() {
    return new int[]{i2cAddr};
  }
}
