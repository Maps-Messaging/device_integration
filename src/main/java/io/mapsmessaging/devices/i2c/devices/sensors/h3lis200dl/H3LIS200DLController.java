/*
 *
 *  Copyright [ 2020 - 2024 ] Matthew Buckton
 *  Copyright [ 2024 - 2026 ] MapsMessaging B.V.
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

package io.mapsmessaging.devices.i2c.devices.sensors.h3lis200dl;

import com.pi4j.exception.Pi4JException;
import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.I2CDeviceScheduler;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import java.io.IOException;

public class H3LIS200DLController extends I2CDeviceController {

  private final H3LIS200DLSensor sensor;

  public H3LIS200DLController() {
    sensor = null;
  }

  protected H3LIS200DLController(AddressableDevice device) throws IOException {
    super(device);
    sensor = new H3LIS200DLSensor(device);
  }

  @Override
  public String getName() {
    return "H3LIS200DL";
  }

  @Override
  public String getDescription() {
    return "High-g three-axis accelerometer";
  }

  @Override
  public boolean canDetect() {
    return true;
  }

  @Override
  public boolean detect(AddressableDevice device) {
    try {
      return device.readRegister(H3LIS200DLSensor.WHO_AM_I) == H3LIS200DLSensor.CHIP_ID;
    } catch (Pi4JException exception) {
      return false;
    }
  }

  @Override
  public I2CDeviceController mount(AddressableDevice device) throws IOException {
    synchronized (I2CDeviceScheduler.getI2cBusLock()) {
      return new H3LIS200DLController(device);
    }
  }

  @Override
  public I2CDevice getDevice() {
    return sensor;
  }

  @Override
  public DeviceType getType() {
    return DeviceType.SENSOR;
  }

  @Override
  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig(buildSchema(sensor));
    config.setComments("ST H3LIS200DL / DFRobot high-g I2C accelerometer");
    config.setTitle(getName());
    config.setVersion(1);
    config.setResourceType("sensor");
    config.setUniqueId(getSchemaId());
    config.setDescription("Three-axis acceleration in m/s², +/-100 g range, 100 Hz sampling");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    return new int[]{0x18, 0x19};
  }
}
