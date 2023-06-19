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
    sensor = new AS3935Sensor(device, 0, -1);
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
    jsonObject.put("Reason", sensor.getReason());
    jsonObject.put("Distance", sensor.getDistance());
    jsonObject.put("MinimumStrikes", sensor.getMinimumStrikes());
    jsonObject.put("Strength", sensor.getStrength());
    jsonObject.put("Registers", sensor.getRegisters());
    return jsonObject.toString(2).getBytes();
  }

  public byte[] getUpdatePayload() {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("Reason", sensor.getReason());
    jsonObject.put("Distance", sensor.getDistance());
    jsonObject.put("MinimumStrikes", sensor.getMinimumStrikes());
    jsonObject.put("Strength", sensor.getStrength());
    jsonObject.put("Registers", sensor.getRegisters());
    return jsonObject.toString(2).getBytes();
  }

  @Override
  public void setPayload(byte[] val) {

  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig();
    config.setComments("i2c device AS3935 is a lightning detector");
    config.setSource("I2C bus address : 0x01, 0x02, 0x03");
    config.setVersion("1.0");
    config.setResourceType("sensor");
    config.setInterfaceDescription("Returns JSON object containing details about the latest detection");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    return new int[]{i2cAddr};
  }
}
