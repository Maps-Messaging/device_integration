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

package io.mapsmessaging.devices.i2c.devices.sensors;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import io.mapsmessaging.devices.i2c.I2CDeviceEntry;
import lombok.Getter;
import org.json.JSONObject;

import java.io.IOException;

public class TLS2561Manager implements I2CDeviceEntry {

  private final int i2cAddr = 0x39;
  private final TLS2561Sensor sensor;

  @Getter
  private final String name = "TLS2561";


  public TLS2561Manager() {
    sensor = null;
  }

  public TLS2561Manager(I2C device) throws IOException {
    sensor = new TLS2561Sensor(device);
  }

  @Override
  public boolean detect() {
    return sensor != null && sensor.isConnected();
  }

  public I2CDeviceEntry mount(I2C device) throws IOException {
    return new TLS2561Manager(device);
  }

  public byte[] getStaticPayload() {
    return "{}".getBytes();
  }

  public byte[] getUpdatePayload() {
    int[] result = sensor.getLevels();
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("ch0", result[0]);
    jsonObject.put("ch1", result[1]);
    jsonObject.put("lux", sensor.calculateLux());
    return jsonObject.toString(2).getBytes();
  }

  @Override
  public void setPayload(byte[] val) {}

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig();
    config.setComments("i2c device TLS2561 light sensor, returns light and IR light levels and computed lux level");
    config.setSource("I2C bus address : 0x39");
    config.setVersion("1.0");
    config.setResourceType("sensor");
    config.setInterfaceDescription("Returns JSON object containing light and IR light levels");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    return new int[]{i2cAddr};
  }
}
