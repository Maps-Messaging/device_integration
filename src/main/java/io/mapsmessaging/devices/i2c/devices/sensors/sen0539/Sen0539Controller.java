/*
 *    Copyright [ 2020 - 2024 ] Matthew Buckton
 *    Copyright [ 2024 - 2025 ] MapsMessaging B.V.
 *
 *    Licensed under the Apache License, Version 2.0 with the Commons Clause
 *    (the "License"); you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at:
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *        https://commonsclause.com/
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License
 */

package io.mapsmessaging.devices.i2c.devices.sensors.sen0539;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;

import java.io.IOException;

public class Sen0539Controller extends I2CDeviceController {

  private static final int I2C_ADDR = 0x64;

  private Sen0539Sensor sensor;

  public Sen0539Controller() {
    this.sensor = null;
  }

  public Sen0539Controller(AddressableDevice device) {
    this.sensor = new Sen0539Sensor(device);
  }

  @Override
  public String getName() {
    return "SEN0539";
  }

  @Override
  public String getDescription() {
    return "Offline Voice Recognition Module (DFRobot DF2301Q)";
  }

  @Override
  public SchemaConfig getSchema() {
    JsonSchemaConfig cfg = new JsonSchemaConfig(buildSchema(sensor));
    cfg.setTitle(getName());
    cfg.setComments(getDescription());
    cfg.setResourceType("sensor");
    cfg.setVersion(1);
    cfg.setUniqueId(getSchemaId());
    cfg.setDescription("I2C address 0x64");
    return cfg;
  }

  @Override
  public int[] getAddressRange() {
    return new int[]{I2C_ADDR};
  }

  @Override
  public boolean detect(AddressableDevice i2cDevice) {
    return sensor != null && sensor.isConnected();
  }

  @Override
  public I2CDeviceController mount(AddressableDevice device) throws IOException {
    return new Sen0539Controller(device);
  }

  @Override
  public I2CDevice getDevice() {
    return sensor;
  }

  @Override
  public DeviceType getType() {
    return getDevice().getType();
  }
}