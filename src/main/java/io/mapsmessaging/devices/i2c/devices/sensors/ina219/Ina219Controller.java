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

package io.mapsmessaging.devices.i2c.devices.sensors.ina219;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.devices.sensors.ina219.registers.*;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Ina219Controller extends I2CDeviceController {

  private static final String ADC_RESOLUTION = "adcResolution";
  private static final String SHUNT_ADC_RESOLUTION = "shuntAdcResolution";
  private static final String BUS_VOLTAGE_RANGE = "busVoltageRange";
  private static final String GAIN_MASK = "gainMask";
  private static final String OPERATING_MODE = "operatingMode";

  private static final int i2cAddr = 0x42;
  private final Ina219Sensor sensor;

  public Ina219Controller() {
    sensor = null;
  }

  @Override
  public String getName() {
    return "INA219";
  }

  @Override
  public String getDescription() {
    return "High Side DC Current Sensor";
  }

  public Ina219Controller(AddressableDevice device) throws IOException {
    super(device);
    sensor = new Ina219Sensor(device);
    sensor.initialize();
  }

  public I2CDevice getDevice() {
    return sensor;
  }

  @Override
  public boolean detect(AddressableDevice i2cDevice) {
    return sensor != null && sensor.isConnected();
  }

  public I2CDeviceController mount(AddressableDevice device) throws IOException {
    return new Ina219Controller(device);
  }

  public DeviceType getType() {
    return getDevice().getType();
  }

  @Override
  public byte[] getDeviceConfiguration() {
    JsonObject jsonObject = new JsonObject();
    if (sensor != null) {
      jsonObject.addProperty(ADC_RESOLUTION, sensor.getAdcResolution().name());
      jsonObject.addProperty(SHUNT_ADC_RESOLUTION, sensor.getShuntADCResolution().name());
      jsonObject.addProperty(BUS_VOLTAGE_RANGE, sensor.getBusVoltageRange().name());
      jsonObject.addProperty(GAIN_MASK, sensor.getGainMask().name());
      jsonObject.addProperty(OPERATING_MODE, sensor.getOperatingMode().name());
    }
    return gson.toJson(jsonObject).getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public byte[] updateDeviceConfiguration(byte[] payload) throws IOException {
    JsonObject jsonObject = JsonParser.parseString(new String(payload, StandardCharsets.UTF_8)).getAsJsonObject();
    JsonObject response = new JsonObject();
    if (sensor == null) return gson.toJson(response).getBytes(StandardCharsets.UTF_8);

    if (jsonObject.has(ADC_RESOLUTION)) {
      ADCResolution adcResolution = ADCResolution.valueOf(jsonObject.get(ADC_RESOLUTION).getAsString());
      sensor.setAdcResolution(adcResolution);
      response.addProperty(ADC_RESOLUTION, adcResolution.name());
    }

    if (jsonObject.has(SHUNT_ADC_RESOLUTION)) {
      ShuntADCResolution shuntAdcResolution = ShuntADCResolution.valueOf(jsonObject.get(SHUNT_ADC_RESOLUTION).getAsString());
      sensor.setShuntADCResolution(shuntAdcResolution);
      response.addProperty(SHUNT_ADC_RESOLUTION, shuntAdcResolution.name());
    }

    if (jsonObject.has(BUS_VOLTAGE_RANGE)) {
      BusVoltageRange busVoltageRange = BusVoltageRange.valueOf(jsonObject.get(BUS_VOLTAGE_RANGE).getAsString());
      sensor.setBusVoltageRange(busVoltageRange);
      response.addProperty(BUS_VOLTAGE_RANGE, busVoltageRange.name());
    }

    if (jsonObject.has(GAIN_MASK)) {
      GainMask gainMask = GainMask.valueOf(jsonObject.get(GAIN_MASK).getAsString());
      sensor.setGainMask(gainMask);
      response.addProperty(GAIN_MASK, gainMask.name());
    }

    if (jsonObject.has(OPERATING_MODE)) {
      OperatingMode operatingMode = OperatingMode.valueOf(jsonObject.get(OPERATING_MODE).getAsString());
      sensor.setOperatingMode(operatingMode);
      response.addProperty(OPERATING_MODE, operatingMode.name());
    }

    sensor.setCalibration();
    response.addProperty("Status", "success");
    return gson.toJson(response).getBytes(StandardCharsets.UTF_8);
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig(buildSchema());
    config.setComments("High Side DC Current Sensor");
    config.setTitle(getName());
    config.setVersion(1);
    config.setResourceType("sensor");
    config.setUniqueId(getSchemaId());
    config.setDescription("Returns json object with current readings from sensor");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    return new int[]{i2cAddr};
  }

  private String buildSchema() {
    JsonObject properties = new JsonObject();

    properties.add(ADC_RESOLUTION, enumSchema(
        ADCResolution.RES_9BIT,
        ADCResolution.RES_10BIT,
        ADCResolution.RES_11BIT,
        ADCResolution.RES_12BIT
    ));

    properties.add(SHUNT_ADC_RESOLUTION, enumSchema(
        ShuntADCResolution.RES_9BIT_1S_84US,
        ShuntADCResolution.RES_10BIT_1S_148US,
        ShuntADCResolution.RES_11BIT_1S_276US,
        ShuntADCResolution.RES_12BIT_1S_532US,
        ShuntADCResolution.RES_12BIT_2S_1060US,
        ShuntADCResolution.RES_12BIT_4S_2130US,
        ShuntADCResolution.RES_12BIT_8S_4260US,
        ShuntADCResolution.RES_12BIT_16S_8510US,
        ShuntADCResolution.RES_12BIT_32S_17MS,
        ShuntADCResolution.RES_12BIT_64S_34MS,
        ShuntADCResolution.RES_12BIT_128S_69MS
    ));

    properties.add(BUS_VOLTAGE_RANGE, enumSchema(
        BusVoltageRange.RANGE_16V,
        BusVoltageRange.RANGE_32V
    ));

    properties.add(GAIN_MASK, enumSchema(
        GainMask.GAIN_1_40MV,
        GainMask.GAIN_2_80MV,
        GainMask.GAIN_4_160MV,
        GainMask.GAIN_8_320MV
    ));

    properties.add(OPERATING_MODE, enumSchema(
        OperatingMode.POWERDOWN,
        OperatingMode.SVOLT_TRIGGERED,
        OperatingMode.BVOLT_TRIGGERED,
        OperatingMode.SANDBVOLT_TRIGGERED,
        OperatingMode.ADCOFF,
        OperatingMode.SVOLT_CONTINUOUS,
        OperatingMode.BVOLT_CONTINUOUS,
        OperatingMode.SANDBVOLT_CONTINUOUS
    ));
    return buildSchema(sensor, properties);
  }

  @SafeVarargs
  private final <T extends Enum<T>> JsonObject enumSchema(T... values) {
    JsonObject schema = new JsonObject();
    schema.addProperty("type", "string");
    JsonArray enums = new JsonArray();
    for (T val : values) {
      enums.add(val.name());
    }
    schema.add("enum", enums);
    return schema;
  }

}