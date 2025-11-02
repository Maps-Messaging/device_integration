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
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import io.mapsmessaging.schemas.model.XRegistrySchemaVersion;

import java.io.IOException;


public class AM2320Controller extends I2CDeviceController {

  private static final int I2C_ADDR = 0x5C;

  private final AM2320Sensor sensor;

  // Used during ServiceLoading
  public AM2320Controller() {
    sensor = null;
  }

  @Override
  public String getName() {
    return "AM2320";
  }

  @Override
  public String getDescription() {
    return "AM2320 Pressure and Temperature Sensor";
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

  @Override
  public byte[] getDeviceConfiguration() {
    return "{}".getBytes();
  }

  public DeviceType getType() {
    return getDevice().getType();
  }

  @Override
  public boolean detect(AddressableDevice i2cDevice) {
    return sensor != null && sensor.isConnected();
  }

  public XRegistrySchemaVersion getSchema() {
    var config = new JsonSchemaConfig(buildSchema(sensor));
    config.setDescription("I2C device AM2320 Pressure and Temperature Sensor https://learn.adafruit.com/adafruit-am2320-temperature-humidity-i2c-sensor");
    config.setComments(getName());
    config.setVersion("1");
    config.setUniqueId(getSchemaId());
    config.setResourceType("sensor");
    config.setInterfaceDescription("Returns JSON object containing Temperature and Pressure");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    return new int[]{I2C_ADDR};
  }


}
