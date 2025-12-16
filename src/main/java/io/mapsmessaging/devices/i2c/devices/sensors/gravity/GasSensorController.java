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

package io.mapsmessaging.devices.i2c.devices.sensors.gravity;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.I2CDeviceScheduler;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.devices.util.UuidGenerator;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;

import java.io.IOException;

public class GasSensorController extends I2CDeviceController {

  private final int[] i2cAddr = {0x74, 0x76};
  private final GasSensor sensor;

  public GasSensorController() {
    sensor = null;
  }

  public GasSensorController(AddressableDevice device) throws IOException {
    super(device);
    sensor = new GasSensor(device);
  }

  @Override
  public boolean detect(AddressableDevice i2cDevice) {
    return sensor != null && sensor.isConnected();
  }

  public I2CDeviceController mount(AddressableDevice device) throws IOException {
    synchronized (I2CDeviceScheduler.getI2cBusLock()) {
      return new GasSensorController(device);
    }
  }

  public I2CDevice getDevice() {
    return sensor;
  }

  public String getName() {
    if (sensor == null) {
      return "Generic Gas Sensor";
    }
    return sensor.getName();
  }

  public DeviceType getType() {
    return getDevice().getType();
  }

  public String getDescription() {
    if (sensor == null) {
      return "Generic Gas Sensor";
    }
    return sensor.getDescription();
  }


  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig(buildSchema(sensor));
    config.setComments(getName());
    config.setVersion(1);
    config.setResourceType("sensor");
    if (sensor != null) {
      config.setUniqueId(UuidGenerator.getInstance().generateUuid(getName() + "-" + sensor.getSensorType().getSku() + "-" + sensor.getSensorType().getGasType()));
      config.setTitle(getName() + "-" + sensor.getSensorType().getGasType());
    } else {
      config.setTitle(getName());
      config.setUniqueId(getSchemaId());
    }
    config.setDescription("Gravity Gas sensor");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    return i2cAddr;
  }

}