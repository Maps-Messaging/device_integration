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

package io.mapsmessaging.devices.i2c.devices.sensors.msa311;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.deviceinterfaces.AbstractRegisterData;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.I2CDeviceScheduler;
import io.mapsmessaging.devices.sensorreadings.ComputationResult;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import lombok.Getter;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class Msa311Controller extends I2CDeviceController {

  private final int i2cAddr = 0x62;
  private final Msa311Sensor sensor;

  @Getter
  private final String name = "MSA311";

  @Getter
  private final String description = "Digital Tri-axial Accelerometer";

  public Msa311Controller() {
    sensor = null;
  }

  public Msa311Controller(I2C device) throws IOException {
    super(device);
    synchronized (I2CDeviceScheduler.getI2cBusLock()) {
      sensor = new Msa311Sensor(device);
    }
  }

  public I2CDevice getDevice() {
    return sensor;
  }


  @Override
  public boolean canDetect() {
    return true;
  }

  @Override
  public boolean detect(I2C i2cDevice) {
    return (Msa311Sensor.getId(i2cDevice) == 0b00010011);
  }

  public I2CDeviceController mount(I2C device) throws IOException {
    return new Msa311Controller(device);
  }

  public byte[] getDeviceConfiguration() throws IOException {
    JSONObject jsonObject = new JSONObject();
    if (sensor != null) {
      ObjectMapper objectMapper = new ObjectMapper();
      try {
        String json = objectMapper.writeValueAsString(sensor.getRegisterMap().getData());
        return json.getBytes();
      }
      catch(IOException ioException){
        ioException.printStackTrace();
        throw ioException;
      }
    }
    return jsonObject.toString(2).getBytes();
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

  public byte[] getDeviceState() throws IOException {
    JSONObject jsonObject = new JSONObject();
    if (sensor != null) {
      List<SensorReading<?>> readings = sensor.getReadings();
      for(SensorReading<?> reading : readings){
        ComputationResult<?> computationResult = reading.getValue();
        if(!computationResult.hasError()){
          jsonObject.put(reading.getName(), computationResult.getResult());
        }
        else{
          jsonObject.put(reading.getName(), computationResult.getError().getMessage());
        }
      }
    }
    return jsonObject.toString(2).getBytes();
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig();
    config.setComments("Digital Tri-axial Accelerometer");
    config.setSource("I2C bus address : " + i2cAddr);
    config.setVersion("1.0");
    config.setResourceType("sensor");
    config.setInterfaceDescription("Digital Tri-axial Accelerometer");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    return new int[]{i2cAddr};
  }
}