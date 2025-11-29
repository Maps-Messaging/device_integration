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

package io.mapsmessaging.devices;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.sensorreadings.ComputationResult;
import io.mapsmessaging.devices.sensorreadings.GroupSensorReading;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import io.mapsmessaging.devices.util.UuidGenerator;
import io.mapsmessaging.schemas.config.SchemaConfig;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public abstract class DeviceController {
  private final AtomicReference<UUID> uuid;

  @Getter
  @Setter
  private boolean raiseExceptionOnError = false;


  protected DeviceController() {
    uuid = new AtomicReference<>();
  }

  protected static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  public UUID getSchemaId() {
    UUID current = uuid.get();
    if (current == null) {
      UUID newUuid = UuidGenerator.getInstance().generateUuid(getName());
      if (uuid.compareAndSet(null, newUuid)) {
        return newUuid;
      } else {
        return uuid.get(); // someone else set it
      }
    }
    return current;
  }


  public abstract String getName();

  public abstract String getDescription();

  public abstract SchemaConfig getSchema();

  public abstract byte[] getDeviceConfiguration() throws IOException;

  public abstract DeviceType getType();

  public abstract byte[] getDeviceState() throws IOException;

  public boolean getRaiseExceptionOnError() {
    return true;
  }

  public abstract byte[] updateDeviceConfiguration(byte[] val) throws IOException;

  public void close() {
    // nothing to do, simple catch all on a fall through
  }

  public String buildSchema(Sensor sensor) {
    return SchemaBuilder.buildSchema(sensor, null);
  }

  public String buildSchema(Sensor sensor, JsonObject additionalValues) {
    return SchemaBuilder.buildSchema(sensor, additionalValues);
  }

  protected void walkSensorReadings(JsonObject root, List<SensorReading<?>> readings) throws IOException {
    for (SensorReading<?> reading : readings) {
      if (reading instanceof GroupSensorReading groupReading) {
        JsonObject readingObject = new JsonObject();
        walkSensorReadings(new JsonObject(), groupReading.getGroupList());
        if (!readingObject.isEmpty()) {
          root.add(reading.getName(), readingObject);
        }
      } else {
        addProperty(reading, root);
      }
    }
  }

  private void addProperty(SensorReading<?> reading, JsonObject jsonObject) throws IOException {
    ComputationResult<?> computationResult = reading.getValue();
    if (!computationResult.hasError()) {
      Object value = computationResult.getResult();
      if (value instanceof Optional<?> optional) {
        if (optional.isEmpty()) {
          return;
        }
        value = optional.get();
      }

      if (value instanceof Number number) {
        jsonObject.addProperty(reading.getName(), number);
      } else if (value instanceof Boolean bool) {
        jsonObject.addProperty(reading.getName(), bool);
      } else if (value instanceof Character character) {
        jsonObject.addProperty(reading.getName(), character);
      } else if (value instanceof String str) {
        jsonObject.addProperty(reading.getName(), str);
      } else {
        // Fallback to stringified JSON
        jsonObject.add(reading.getName(), gson.toJsonTree(value));
      }
    } else {
      if (raiseExceptionOnError) {
        throw new IOException(computationResult.getError());
      }
      jsonObject.addProperty(reading.getName(), computationResult.getError().getMessage());
    }
    jsonObject.addProperty("timestamp", Instant.now().toString());
  }

  protected byte[] convert(JsonObject jsonObject) {
    return gson.toJson(jsonObject).getBytes(StandardCharsets.UTF_8);
  }

}
