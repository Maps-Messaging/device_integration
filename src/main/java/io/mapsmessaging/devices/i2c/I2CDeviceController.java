/*
 *
 *  Copyright [ 2020 - 2024 ] Matthew Buckton
 *  Copyright [ 2024 - 2026 ] MapsMessaging B.V.
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

package io.mapsmessaging.devices.i2c;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import io.mapsmessaging.devices.DeviceController;
import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.devices.io.SerialisationHelper;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public abstract class I2CDeviceController extends DeviceController {

  @Getter
  @Setter
  private static boolean timestampReadings = true;

  protected static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  private boolean closed;
  private final int mountedAddress;
  private final SerialisationHelper serialisationHelper = new SerialisationHelper();

  protected I2CDeviceController() {
    this(null);
  }

  protected I2CDeviceController(AddressableDevice device) {
    if (device != null) {
      mountedAddress = device.getDevice();
    } else {
      mountedAddress = -1;
    }
  }

  @Override
  public String buildSchema(Sensor sensor) {
    return normalizeStateSchema(super.buildSchema(sensor));
  }

  @Override
  public String buildSchema(Sensor sensor, JsonObject additionalValues) {
    return normalizeStateSchema(super.buildSchema(sensor, additionalValues));
  }

  private String normalizeStateSchema(String source) {
    JsonObject schema = JsonParser.parseString(source).getAsJsonObject();
    normalizeReadingProperties(schema);
    return gson.toJson(schema);
  }

  private void normalizeReadingProperties(JsonObject schema) {
    // A failed I2C read may return an error message instead of a value, or the
    // controller may return an empty state if its entire read operation fails.
    schema.remove("required");
    JsonObject properties = schema.getAsJsonObject("properties");
    if (properties == null) {
      return;
    }
    for (Map.Entry<String, JsonElement> property : properties.entrySet()) {
      if ("timestamp".equals(property.getKey()) || !property.getValue().isJsonObject()) {
        continue;
      }
      JsonObject value = property.getValue().getAsJsonObject();
      if (value.has("properties") && value.get("properties").isJsonObject()) {
        normalizeReadingProperties(value);
      }
      String type = value.has("type") && value.get("type").isJsonPrimitive()
          ? value.get("type").getAsString() : "";
      if (!"string".equals(type) || value.has("format")) {
        JsonObject error = new JsonObject();
        error.addProperty("type", "string");
        error.addProperty("description", "Device error message when a reading is unavailable");
        JsonArray alternatives = new JsonArray();
        alternatives.add(value.deepCopy());
        alternatives.add(error);
        JsonObject optionalResult = new JsonObject();
        optionalResult.add("anyOf", alternatives);
        properties.add(property.getKey(), optionalResult);
      }
    }
  }

  @Override
  public void close() {
    synchronized (this) {
      if (!closed) {
        closed = true;
        I2CDevice device = getDevice();
        if (device != null) {
          device.close();
        }
      }
    }
  }

  @Override
  public byte[] updateDeviceConfiguration(byte[] val) throws IOException {
    I2CDevice device = getDevice();
    if (device != null) {
      Map<Integer, RegisterData> map2 = serialisationHelper.deserialise(val);
      device.getRegisterMap().setData(map2);
    }
    return ("{}").getBytes();
  }

  @Override
  public byte[] getDeviceConfiguration() throws IOException {
    I2CDevice device = getDevice();
    JsonObject jsonObject = new JsonObject();
    if (device != null) {
      return serialisationHelper.serialise(device.getRegisterMap().getData());
    }
    return convert(jsonObject);
  }

  @Override
  public byte[] getDeviceState() throws IOException {
    try {
      I2CDevice device = getDevice();
      JsonObject jsonObject = new JsonObject();
      if (device instanceof Sensor sensor) {
        List<SensorReading<?>> readings = sensor.getReadings();
        walkSensorReadings(jsonObject, readings);
      }
      return convert(jsonObject);
    } catch (Exception e) {
      // Log this
    }
    return "{}".getBytes();
  }

  public boolean canDetect() {
    return false;
  }

  public abstract I2CDeviceController mount(AddressableDevice device) throws IOException;

  public abstract int[] getAddressRange();

  public abstract boolean detect(AddressableDevice i2cDevice);

  public abstract I2CDevice getDevice();

  protected byte[] emptyJson(){
    return "{}".getBytes(StandardCharsets.UTF_8);
  }
}
