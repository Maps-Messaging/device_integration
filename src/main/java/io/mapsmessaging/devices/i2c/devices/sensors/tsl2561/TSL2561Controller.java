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

package io.mapsmessaging.devices.i2c.devices.sensors.tsl2561;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.devices.util.UuidGenerator;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import lombok.Getter;

import java.io.IOException;

public class TSL2561Controller extends I2CDeviceController {

  private final TSL2561Sensor sensor;

  @Getter
  private final String name = "TLS2561";
  @Getter
  private final String description = "Light sensor, returns light and IR light levels and computed lux level";

  public TSL2561Controller() {
    sensor = null;
  }

  public TSL2561Controller(AddressableDevice device) throws IOException {
    super(device);
    sensor = new TSL2561Sensor(device);
  }

  public I2CDevice getDevice() {
    return sensor;
  }
  public DeviceType getType(){
    return getDevice().getType();
  }

  @Override
  public boolean detect(AddressableDevice i2cDevice) {
    return sensor != null && sensor.isConnected();
  }

  public I2CDeviceController mount(AddressableDevice device) throws IOException {
    return new TSL2561Controller(device);
  }


  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig();
    config.setComments("i2c device TLS2561 light sensor, returns light and IR light levels and computed lux level");
    config.setSource(getName());
    config.setVersion("1.0");
    config.setResourceType("sensor");
    config.setUniqueId(UuidGenerator.getInstance().generateUuid(getName()));
    config.setInterfaceDescription("Returns JSON object containing light and IR light levels");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    int i2cAddr = 0x39;
    return new int[]{i2cAddr};
  }
}
