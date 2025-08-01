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

package io.mapsmessaging.devices.i2c.devices.sensors.sht31;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import lombok.Getter;

import java.io.IOException;

public class Sht31Controller extends I2CDeviceController {

  private final Sht31Sensor sensor;

  @Getter
  private final String name = "SHT31-D";
  @Getter
  private final String description = "Humidity and Temperature Sensor";

  public Sht31Controller() {
    sensor = null;
  }

  public Sht31Controller(AddressableDevice device) throws IOException {
    super(device);
    sensor = new Sht31Sensor(device);
  }

  public I2CDevice getDevice() {
    return sensor;
  }

  public DeviceType getType() {
    return getDevice().getType();
  }

  @Override
  public boolean detect(AddressableDevice i2cDevice) {
    return sensor != null && sensor.isConnected();
  }

  public I2CDeviceController mount(AddressableDevice device) throws IOException {
    return new Sht31Controller(device);
  }


  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig(buildSchema(sensor));
    config.setComments(description);
    config.setTitle(getName());
    config.setVersion(1);
    config.setResourceType("sensor");
    config.setUniqueId(getSchemaId());
    config.setInterfaceDescription("Returns JSON object containing light and IR light levels");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    int i2cAddr = 0x44;
    return new int[]{i2cAddr};
  }
}
