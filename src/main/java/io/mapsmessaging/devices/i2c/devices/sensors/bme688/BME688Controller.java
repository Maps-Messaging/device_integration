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

package io.mapsmessaging.devices.i2c.devices.sensors.bme688;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.I2CDeviceScheduler;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;

import java.io.IOException;

public class BME688Controller extends I2CDeviceController {

  private static final int I2C_ADDR = 0x77;
  private final BME688Sensor sensor;

  public BME688Controller() {
    sensor = null;
  }

  @Override
  public String getName() {
    return "BME688";
  }

  @Override
  public String getDescription() {
    return "VOC, Humidity, Pressure and Temperature Module";
  }

  protected BME688Controller(AddressableDevice device) throws IOException {
    super(device);
    sensor = new BME688Sensor(device);
    sensor.startForceMode();
  }

  public I2CDevice getDevice() {
    return sensor;
  }

  @Override
  public boolean detect(AddressableDevice i2cDevice) {
    return sensor != null && sensor.isConnected();
  }

  public I2CDeviceController mount(AddressableDevice device) throws IOException {
    synchronized (I2CDeviceScheduler.getI2cBusLock()) {
      return new BME688Controller(device);
    }
  }

  public DeviceType getType() {
    return getDevice().getType();
  }


  @Override
  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig(buildSchema(sensor));
    config.setComments("I2C device BME688 VOC, Pressure, Temperature and Humidity Sensor");
    config.setTitle(getName());
    config.setVersion(1);
    config.setUniqueId(getSchemaId());
    config.setResourceType("sensor");
    config.setInterfaceDescription("Returns JSON object containing Temperature, Humidity, Pressure, Gas Resistance, Heater status and Gas mode");
    return config;
  }


  @Override
  public int[] getAddressRange() {
    return new int[]{I2C_ADDR};
  }

}
