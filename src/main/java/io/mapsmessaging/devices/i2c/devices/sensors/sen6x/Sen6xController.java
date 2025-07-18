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

package io.mapsmessaging.devices.i2c.devices.sensors.sen6x;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import lombok.Getter;

import java.io.IOException;

public class Sen6xController extends I2CDeviceController {

  private final Sen6xSensor sensor;

  @Getter
  private final String name = "SEN6x";
  @Getter
  private final String description = "Air Quality Sensor for PM, RH/T, VOC, Nox, CO2, HCOH";


  public Sen6xController() {
    sensor = null;
  }

  public Sen6xController(AddressableDevice device) throws IOException {
    super(device);
    sensor = new Sen6xSensor(device);
  }

  public I2CDevice getDevice() {
    return sensor;
  }

  public DeviceType getType() {
    return getDevice().getType();
  }

  @Override
  public boolean canDetect() {
    return true;
  }

  @Override
  public boolean detect(AddressableDevice i2cDevice) {
    return Sen6xSensor.detect(i2cDevice);
  }

  public I2CDeviceController mount(AddressableDevice device) throws IOException {
    return new Sen6xController(device);
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config =  new JsonSchemaConfig(buildSchema(sensor));
    config.setComments(description);
    config.setTitle(getName());
    config.setVersion(1);
    config.setResourceType("sensor");
    config.setUniqueId(getSchemaId());
    config.setInterfaceDescription("Returns Air Quality valies in JSON");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    int i2cAddr = 0x6B;
    return new int[]{i2cAddr};
  }
}
