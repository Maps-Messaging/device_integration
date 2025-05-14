/*
 *
 *  Copyright [ 2020 - 2024 ] [Matthew Buckton]
 *  Copyright [ 2024 - 2025.  ] [Maps Messaging B.V.]
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package io.mapsmessaging.devices.i2c.devices.sensors.lps35;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.I2CDeviceScheduler;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.devices.util.UuidGenerator;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import lombok.Getter;

import java.io.IOException;

public class Lps35Controller extends I2CDeviceController {

  private final Lps35Sensor sensor;

  @Getter
  private final String name = "LPS35";
  @Getter
  private final String description = "Pressure and Temperature sensor";


  public Lps35Controller() {
    sensor = null;
  }

  public Lps35Controller(AddressableDevice device) throws IOException {
    super(device);
    synchronized (I2CDeviceScheduler.getI2cBusLock()) {
      sensor = new Lps35Sensor(device);
    }
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
    return (Lps35Sensor.getId(i2cDevice) == 0b10110001);
  }

  public I2CDeviceController mount(AddressableDevice device) throws IOException {
    return new Lps35Controller(device);
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig(buildSchema(sensor));
    config.setComments("i2c device LPS35 pressure sensor: 260-1260 hPa");
    config.setSource(getName());
    config.setVersion("1.0");
    config.setUniqueId(UuidGenerator.getInstance().generateUuid(getName()));
    config.setResourceType("sensor");
    config.setInterfaceDescription("Returns JSON object containing pressure and temperature");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    int i2cAddr = 0x5E;
    return new int[]{i2cAddr};
  }
}
