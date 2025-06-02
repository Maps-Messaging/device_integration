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

package io.mapsmessaging.devices.i2c.devices.sensors.ina219;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.devices.sensors.ina219.registers.*;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import lombok.Getter;
import org.everit.json.schema.EnumSchema;
import org.everit.json.schema.ObjectSchema;
import org.json.JSONObject;

import java.io.IOException;

import static io.mapsmessaging.devices.i2c.devices.sensors.ina219.Constants.INA219_ADDRESS;

public class Ina219Controller extends I2CDeviceController {

  private final int i2cAddr = INA219_ADDRESS;
  private final Ina219Sensor sensor;

  @Getter
  private final String name = "INA219";

  @Getter
  private final String description = "High Side DC Current Sensor";

  public Ina219Controller() {
    sensor = null;
  }

  public Ina219Controller(AddressableDevice device) throws IOException {
    super(device);
    sensor = new Ina219Sensor(device);
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

  public byte[] getDeviceConfiguration() {
    JSONObject jsonObject = new JSONObject();
    if (sensor != null) {
      jsonObject.put("adcResolution", sensor.getAdcResolution().name());
      jsonObject.put("shuntAdcResolution", sensor.getShuntADCResolution().name());
      jsonObject.put("busVoltageRange", sensor.getBusVoltageRange().name());
      jsonObject.put("gainMask", sensor.getGainMask().name());
      jsonObject.put("operatingMode", sensor.getOperatingMode().name());
    }
    return jsonObject.toString(2).getBytes();
  }

  @Override
  public byte[] updateDeviceConfiguration(byte[] payload) throws IOException {
    JSONObject jsonObject = new JSONObject(new String(payload));
    JSONObject response = new JSONObject();
    if (sensor == null) return response.toString(2).getBytes();
    if (jsonObject.has("adcResolution")) {
      String adcResolutionStr = jsonObject.getString("adcResolution");
      ADCResolution adcResolution = ADCResolution.valueOf(adcResolutionStr);
      sensor.setAdcResolution(adcResolution);
      response.put("adcResolution", adcResolution.name());
    }

    if (jsonObject.has("shuntAdcResolution")) {
      String shuntAdcResolutionStr = jsonObject.getString("shuntAdcResolution");
      ShuntADCResolution shuntAdcResolution = ShuntADCResolution.valueOf(shuntAdcResolutionStr);
      sensor.setShuntADCResolution(shuntAdcResolution);
      response.put("shuntAdcResolution", shuntAdcResolution.name());
    }

    if (jsonObject.has("busVoltageRange")) {
      String busVoltageRangeStr = jsonObject.getString("busVoltageRange");
      BusVoltageRange busVoltageRange = BusVoltageRange.valueOf(busVoltageRangeStr);
      sensor.setBusVoltageRange(busVoltageRange);
      response.put("busVoltageRange", busVoltageRange.name());
    }

    if (jsonObject.has("gainMask")) {
      String gainMaskStr = jsonObject.getString("gainMask");
      GainMask gainMask = GainMask.valueOf(gainMaskStr);
      sensor.setGainMask(gainMask);
      response.put("gainMask", gainMask.name());
    }

    if (jsonObject.has("operatingMode")) {
      String operatingModeStr = jsonObject.getString("operatingMode");
      OperatingMode operatingMode = OperatingMode.valueOf(operatingModeStr);
      sensor.setOperatingMode(operatingMode);
      response.put("operatingMode", operatingMode.name());
    }
    sensor.setCalibration();
    response.put("Status", "success");
    return response.toString(2).getBytes();
  }

  public byte[] getDeviceState() throws IOException {
    JSONObject jsonObject = new JSONObject();
    if (sensor != null) {
      jsonObject.put("current", sensor.getCurrent());
      jsonObject.put("shuntVoltage", sensor.getShuntVoltage());
      jsonObject.put("busVoltage", sensor.getBusVoltage());
      jsonObject.put("power", sensor.getPower());
    }
    return jsonObject.toString(2).getBytes();
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig(buildSchema());
    config.setComments("High Side DC Current Sensor");
    config.setTitle(getName());
    config.setVersion("1.0");
    config.setResourceType("sensor");
    config.setUniqueId(getSchemaId());
    config.setInterfaceDescription("Returns json object with current readings from sensor");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    return new int[]{i2cAddr};
  }

  private String buildSchema() {
    ObjectSchema staticSchema = ObjectSchema.builder()
        .addPropertySchema("adcResolution", EnumSchema.builder()
            .possibleValue(ADCResolution.RES_9BIT.name())
            .possibleValue(ADCResolution.RES_10BIT.name())
            .possibleValue(ADCResolution.RES_11BIT.name())
            .possibleValue(ADCResolution.RES_12BIT.name())
            .build())
        .addPropertySchema("shuntAdcResolution", EnumSchema.builder()
            .possibleValue(ShuntADCResolution.RES_9BIT_1S_84US.name())
            .possibleValue(ShuntADCResolution.RES_10BIT_1S_148US.name())
            .possibleValue(ShuntADCResolution.RES_11BIT_1S_276US.name())
            .possibleValue(ShuntADCResolution.RES_12BIT_1S_532US.name())
            .possibleValue(ShuntADCResolution.RES_12BIT_2S_1060US.name())
            .possibleValue(ShuntADCResolution.RES_12BIT_4S_2130US.name())
            .possibleValue(ShuntADCResolution.RES_12BIT_8S_4260US.name())
            .possibleValue(ShuntADCResolution.RES_12BIT_16S_8510US.name())
            .possibleValue(ShuntADCResolution.RES_12BIT_32S_17MS.name())
            .possibleValue(ShuntADCResolution.RES_12BIT_64S_34MS.name())
            .possibleValue(ShuntADCResolution.RES_12BIT_128S_69MS.name())
            .build())
        .addPropertySchema("busVoltageRange", EnumSchema.builder()
            .possibleValue(BusVoltageRange.RANGE_16V.name())
            .possibleValue(BusVoltageRange.RANGE_32V.name())
            .build())
        .addPropertySchema("gainMask", EnumSchema.builder()
            .possibleValue(GainMask.GAIN_1_40MV.name())
            .possibleValue(GainMask.GAIN_2_80MV.name())
            .possibleValue(GainMask.GAIN_4_160MV.name())
            .possibleValue(GainMask.GAIN_8_320MV.name())
            .build())
        .addPropertySchema("operatingMode", EnumSchema.builder()
            .possibleValue(OperatingMode.POWERDOWN.name())
            .possibleValue(OperatingMode.SVOLT_TRIGGERED.name())
            .possibleValue(OperatingMode.BVOLT_TRIGGERED.name())
            .possibleValue(OperatingMode.SANDBVOLT_TRIGGERED.name())
            .possibleValue(OperatingMode.ADCOFF.name())
            .possibleValue(OperatingMode.SVOLT_CONTINUOUS.name())
            .possibleValue(OperatingMode.BVOLT_CONTINUOUS.name())
            .possibleValue(OperatingMode.SANDBVOLT_CONTINUOUS.name())
            .build())
        .build();

    return buildSchema(sensor, staticSchema);
  }

}