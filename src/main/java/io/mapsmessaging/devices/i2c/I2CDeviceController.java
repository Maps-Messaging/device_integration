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

package io.mapsmessaging.devices.i2c;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.mapsmessaging.devices.DeviceController;
import io.mapsmessaging.devices.deviceinterfaces.RegisterData;
import io.mapsmessaging.devices.deviceinterfaces.Sensor;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.devices.io.RegisterDataDeserializer;
import io.mapsmessaging.devices.io.RegisterDataWrapper;
import io.mapsmessaging.devices.sensorreadings.ComputationResult;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import lombok.Getter;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public abstract class I2CDeviceController implements DeviceController {

  @Getter
  private final int mountedAddress;

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
      ObjectMapper mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      SimpleModule module = new SimpleModule();
      module.addDeserializer(RegisterData.class, new RegisterDataDeserializer());
      mapper.registerModule(module);
      RegisterDataWrapper wrapper2 = mapper.readValue(new String(val), RegisterDataWrapper.class);
      Map<Integer, RegisterData> map2 = wrapper2.getMap();
      System.err.println(map2);
      //device.getRegisterMap().setData(data);
    }
    return ("{}").getBytes();
  }

  @Override
  public byte[] getDeviceConfiguration() throws IOException {
    I2CDevice device = getDevice();
    JSONObject jsonObject = new JSONObject();
    if (device != null) {
      ObjectMapper mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

      SimpleModule module = new SimpleModule();
      module.addDeserializer(RegisterData.class, new RegisterDataDeserializer());
      mapper.registerModule(module);
      Map<Integer, RegisterData> map = device.getRegisterMap().getData();
      RegisterDataWrapper wrapper = new RegisterDataWrapper(map);

      // serialize
      String json = mapper.writeValueAsString(wrapper);
      System.err.println(json);
      return json.getBytes();
    }
    return jsonObject.toString(2).getBytes();
  }

  @Override
  public byte[] getDeviceState() throws IOException {
    I2CDevice device = getDevice();
    JSONObject jsonObject = new JSONObject();
    if (device instanceof Sensor) {
      List<SensorReading<?>> readings = ((Sensor)device).getReadings();
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

  public boolean canDetect() {
    return false;
  }

  public abstract I2CDeviceController mount(AddressableDevice device) throws IOException;

  public abstract int[] getAddressRange();

  public abstract boolean detect(AddressableDevice i2cDevice);

  public abstract I2CDevice getDevice();

}
