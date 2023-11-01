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

package io.mapsmessaging.devices.spi.devices.mcp3y0x;

import com.pi4j.context.Context;
import com.pi4j.io.spi.Spi;
import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.NamingConstants;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import io.mapsmessaging.devices.spi.SpiDeviceController;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import lombok.Getter;
import lombok.Setter;
import org.everit.json.schema.ArraySchema;
import org.everit.json.schema.NumberSchema;
import org.everit.json.schema.ObjectSchema;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Mcp3y0xController extends SpiDeviceController {
  private static final String NAME = "Mcp3y0x";

  private final Mcp3y0xDevice device;
  private boolean raiseExceptionOnError = false;

  public Mcp3y0xController() {
    device = null;
  }

  public Mcp3y0xController(Mcp3y0xDevice device) {
    this.device = device;
  }

  @Override
  public SpiDeviceController mount(Context pi4j, Map<String, String> map) {
    int spiBus = Integer.parseInt(map.get("spiBus"));
    int chipSelectInt = Integer.parseInt(map.get("spiChipSelect"));
    int spiModeInt = Integer.parseInt(map.get("spiMode"));

    int resolution = Integer.parseInt(map.get("resolution"));
    int channels = Integer.parseInt(map.get("channels"));

    String description = "Microchip Technology Analog to Digital " + channels + " channel " + resolution + " bit convertor";
    Spi spi = createDevice(pi4j, getName(), description, spiBus, getChipSelect(chipSelectInt), getMode(spiModeInt));
    return new Mcp3y0xController(new Mcp3y0xDevice(spi, resolution, channels));
  }

  public DeviceType getType(){
    return device.getType();
  }

  @Override
  public String getName() {
    if (device == null) return NAME;
    return device.getName();
  }

  @Override
  public String getDescription() {
    if (device == null) return NAME;
    return device.getDescription();
  }

  public List<SensorReading<?>> getSensors() {
    return device.getSensors();
  }

  public byte[] getDeviceConfiguration() {
    JSONObject jsonObject = new JSONObject();
    if (device != null) {
      jsonObject.put("resolution", device.getBits());
      jsonObject.put("channels", device.getChannels());
      jsonObject.put("dutyCycle", device.getDutyCycle());
    }
    return jsonObject.toString(2).getBytes();
  }

  public byte[] getDeviceState() {
    JSONObject jsonObject = new JSONObject();
    JSONArray jsonArray = new JSONArray();
    if (device != null) {
      for (short x = 0; x < device.channels; x++) {
        jsonArray.put(device.readFromChannel(false, x));
      }
    }
    jsonObject.put("current", jsonArray);
    return jsonObject.toString(2).getBytes();
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig(buildSchema());
    config.setComments("SPI device Analog to Digital convertor");
    config.setSource(getName());
    config.setVersion("1.0");
    config.setResourceType("sensor");
    config.setInterfaceDescription("Returns JSON object containing the latest readings from all channels");
    return config;
  }

  private String buildSchema() {
    ObjectSchema.Builder staticSchema = ObjectSchema.builder()
        .addPropertySchema("resolution",
            NumberSchema.builder()
                .description("The number of bits of resolution that the result has")
                .build()
        )
        .addPropertySchema("channels",
            NumberSchema.builder()
                .description("Number of ADC channels available")
                .build()
        )
        .addPropertySchema("dutyCycle",
            NumberSchema.builder()
                .description("Number of times per second that the device can be read")
                .build()
        );

    ObjectSchema.Builder updateSchema = ObjectSchema.builder()
        .addPropertySchema("current",
            ArraySchema.builder()
                .minItems(0)
                .maxItems(device == null ? 8 : device.channels)
                .description("Current values of all channels on the ADC")
                .build()
        );

    ObjectSchema.Builder schemaBuilder = ObjectSchema.builder();
    schemaBuilder
        .addPropertySchema(NamingConstants.SENSOR_DATA_SCHEMA, updateSchema.build())
        .addPropertySchema(NamingConstants.DEVICE_STATIC_DATA_SCHEMA, staticSchema.build())
        .description("Analog to digital convertor")
        .title(NAME);
    return schemaToString(schemaBuilder.build());
  }

}
