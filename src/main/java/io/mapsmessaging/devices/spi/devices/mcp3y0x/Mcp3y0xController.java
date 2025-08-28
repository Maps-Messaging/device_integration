/*
 *
 *  Copyright [ 2020 - 2024 ] Matthew Buckton
 *  Copyright [ 2024 - 2025 ] MapsMessaging B.V.
 *
 *  Licensed under the Apache License, Version 2.0 with the Commons Clause
 *  (the "License"); you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      https://commonsclause.com/
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */

package io.mapsmessaging.devices.spi.devices.mcp3y0x;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.pi4j.context.Context;
import com.pi4j.io.spi.Spi;
import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import io.mapsmessaging.devices.spi.SpiDeviceController;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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

  public DeviceType getType() {
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
    return device != null ? device.getReadings() : new ArrayList<>();
  }

  public byte[] getDeviceConfiguration() {
    JsonObject jsonObject = new JsonObject();
    if (device != null) {
      jsonObject.addProperty("resolution", device.getBits());
      jsonObject.addProperty("channels", device.getChannels());
      jsonObject.addProperty("dutyCycle", Mcp3y0xDevice.getDutyCycle());
    }
    return gson.toJson(jsonObject).getBytes(StandardCharsets.UTF_8);
  }

  public byte[] getDeviceState() {
    JsonObject jsonObject = new JsonObject();
    JsonArray jsonArray = new JsonArray();
    if (device != null) {
      for (short x = 0; x < device.channels; x++) {
        jsonArray.add(device.readFromChannel(false, x));
      }
    }
    jsonObject.add("current", jsonArray);
    return gson.toJson(jsonObject).getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public byte[] updateDeviceConfiguration(byte[] val) throws IOException {
    return new byte[0];
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig(buildSchema());
    config.setComments("SPI device Analog to Digital convertor");
    config.setTitle(getName());
    config.setVersion(1);
    config.setResourceType("sensor");
    config.setUniqueId(getSchemaId());
    config.setInterfaceDescription("Returns JSON object containing the latest readings from all channels");
    return config;
  }

  private String buildSchema() {
    JsonObject resolution = new JsonObject();
    resolution.addProperty("type", "number");
    resolution.addProperty("description", "ADC resolution in bits");

    JsonObject channels = new JsonObject();
    channels.addProperty("type", "number");
    channels.addProperty("description", "Number of ADC channels");

    JsonObject dutyCycle = new JsonObject();
    dutyCycle.addProperty("type", "number");
    dutyCycle.addProperty("description", "Read rate in Hz");

    JsonObject properties = new JsonObject();
    properties.add("resolution", resolution);
    properties.add("channels", channels);
    properties.add("dutyCycle", dutyCycle);

    JsonObject schema = new JsonObject();
    schema.addProperty("type", "object");
    schema.add("properties", properties);

    return buildSchema(device, schema);
  }

}
