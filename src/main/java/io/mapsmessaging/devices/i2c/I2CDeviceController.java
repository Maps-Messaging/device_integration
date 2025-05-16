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

package io.mapsmessaging.devices.i2c;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.mapsmessaging.devices.DeviceController;
import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.devices.io.SerialisationHelper;
import io.mapsmessaging.devices.sensorreadings.ComputationResult;
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

  protected static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  private final int mountedAddress;
  private final SerialisationHelper serialisationHelper = new SerialisationHelper();

  private boolean raiseExceptionOnError = false;

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
    I2CDevice device = getDevice();
    JsonObject jsonObject = new JsonObject();
    if (device instanceof Sensor) {
      List<SensorReading<?>> readings = ((Sensor) device).getReadings();
      for (SensorReading<?> reading : readings) {
        addProperty(reading, jsonObject);
      }
    }
    return convert(jsonObject);
  }

  private void addProperty(SensorReading<?> reading, JsonObject jsonObject) throws IOException {
    ComputationResult<?> computationResult = reading.getValue();
    if (!computationResult.hasError()) {
      Object value = computationResult.getResult();
      if (value instanceof Number) {
        jsonObject.addProperty(reading.getName(), (Number) value);
      } else if (value instanceof Boolean) {
        jsonObject.addProperty(reading.getName(), (Boolean) value);
      } else if (value instanceof Character) {
        jsonObject.addProperty(reading.getName(), (Character) value);
      } else if (value instanceof String) {
        jsonObject.addProperty(reading.getName(), (String) value);
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

  }

  public boolean canDetect() {
    return false;
  }

  public abstract I2CDeviceController mount(AddressableDevice device) throws IOException;

  public abstract int[] getAddressRange();

  public abstract boolean detect(AddressableDevice i2cDevice);

  public abstract I2CDevice getDevice();


  protected byte[] convert(JsonObject jsonObject) {
    return gson.toJson(jsonObject).getBytes(StandardCharsets.UTF_8);
  }

}
