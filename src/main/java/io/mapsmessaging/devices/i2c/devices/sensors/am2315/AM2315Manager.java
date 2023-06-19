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

package io.mapsmessaging.devices.i2c.devices.sensors.am2315;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import io.mapsmessaging.devices.i2c.I2CDeviceEntry;
import lombok.Getter;
import org.json.JSONObject;

import java.io.IOException;

public class AM2315Manager implements I2CDeviceEntry {

  private final int i2cAddr = 0x5C;
  private final AM2315Sensor sensor;
  @Getter
  private final String name = "AM2315";

  public AM2315Manager() {
    sensor = null;
  }

  protected AM2315Manager(I2C device) throws IOException {
    sensor = new AM2315Sensor(device);
  }

  @Override
  public boolean detect() {
    return sensor != null && sensor.isConnected();
  }

  public I2CDeviceEntry mount(I2C device) throws IOException {
    return new AM2315Manager(device);
  }

  public byte[] getStaticPayload() {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("Model", sensor.getModel());
    jsonObject.put("Status", sensor.getStatus());
    jsonObject.put("Version", sensor.getVersion());
    return jsonObject.toString(2).getBytes();
  }

  public byte[] getUpdatePayload() {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("temperature", sensor.getTemperature());
    jsonObject.put("humidity", sensor.getHumidity());
    return jsonObject.toString(2).getBytes();
  }

  @Override
  public void setPayload(byte[] val) {

  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig();
    config.setComments("i2c device AM2315 encased Temperature and Humidity Sensor https://www.adafruit.com/product/1293");
    config.setSource("I2C bus address : 0x5C");
    config.setVersion("1.0");
    config.setResourceType("sensor");
    config.setInterfaceDescription("Returns JSON object containing Temperature, Humidity, Model, Status and Version");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    return new int[]{i2cAddr};
  }
}
