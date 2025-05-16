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

package io.mapsmessaging.devices.i2c.devices.sensors.am2320;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import lombok.Getter;
import org.json.JSONObject;

import java.io.IOException;

public class AM2320Controller extends I2CDeviceController {

  private final int i2cAddr = 0x5C;
  private final AM2320Sensor sensor;
  @Getter
  private final String name = "AM2320";
  @Getter
  private final String description = "AM2320 Pressure and Temperature Sensor";

  // Used during ServiceLoading
  public AM2320Controller() {
    sensor = null;
  }

  protected AM2320Controller(AddressableDevice device) throws IOException {
    super(device);
    sensor = new AM2320Sensor(device);
  }

  public I2CDevice getDevice() {
    return sensor;
  }

  public I2CDeviceController mount(AddressableDevice device) throws IOException {
    return new AM2320Controller(device);
  }

  public byte[] getDeviceConfiguration() {
    return "{}".getBytes();
  }

  public DeviceType getType() {
    return getDevice().getType();
  }

  public byte[] getDeviceState() throws IOException {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("humidity", sensor.getHumidity());
    jsonObject.put("temperature", sensor.getTemperature());
    return jsonObject.toString(2).getBytes();
  }

  @Override
  public boolean detect(AddressableDevice i2cDevice) {
    return sensor != null && sensor.isConnected();
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig(buildSchema(sensor));
    config.setComments("I2C device AM2320 Pressure and Temperature Sensor https://learn.adafruit.com/adafruit-am2320-temperature-humidity-i2c-sensor");
    config.setSource(getName());
    config.setVersion("1.0");
    config.setUniqueId(getSchemaId());
    config.setResourceType("sensor");
    config.setInterfaceDescription("Returns JSON object containing Temperature and Pressure");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    return new int[]{i2cAddr};
  }


}
