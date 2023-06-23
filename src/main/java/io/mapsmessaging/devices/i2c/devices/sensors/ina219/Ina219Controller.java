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

package io.mapsmessaging.devices.i2c.devices.sensors.ina219;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.i2c.I2CDeviceEntry;
import io.mapsmessaging.devices.i2c.devices.sensors.ds3231.Ds3231Controller;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import lombok.Getter;
import org.json.JSONObject;

import static io.mapsmessaging.devices.i2c.devices.sensors.ina219.Constants.INA219_ADDRESS;

public class Ina219Controller implements I2CDeviceEntry {

  private final int i2cAddr = INA219_ADDRESS;
  private final Ina219Sensor sensor;

  @Getter
  private final String name = "DS3231";


  public Ina219Controller() {
    sensor = null;
  }

  public Ina219Controller(I2C device) {
    sensor = new Ina219Sensor(device);
  }

  @Override
  public boolean detect() {
    return sensor != null && sensor.isConnected();
  }

  public I2CDeviceEntry mount(I2C device) {
    return new Ds3231Controller(device);
  }

  public byte[] getStaticPayload() {
    JSONObject jsonObject = new JSONObject();
    if (sensor != null) {

    }
    return jsonObject.toString(2).getBytes();
  }

  public byte[] getUpdatePayload() {
    JSONObject jsonObject = new JSONObject();
    if (sensor != null) {

    }
    return jsonObject.toString(2).getBytes();
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig();
    config.setComments("i2c RTC");
    config.setSource("I2C bus address : 0x68");
    config.setVersion("1.0");
    config.setResourceType("rtc");
    config.setInterfaceDescription("Returns JSON object containing the latest rtc");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    return new int[]{i2cAddr};
  }
}