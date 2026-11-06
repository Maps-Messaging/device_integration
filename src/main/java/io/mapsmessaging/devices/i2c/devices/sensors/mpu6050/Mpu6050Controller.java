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

package io.mapsmessaging.devices.i2c.devices.sensors.mpu6050;

import com.pi4j.exception.Pi4JException;
import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.I2CDeviceScheduler;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import java.io.IOException;

public class Mpu6050Controller extends I2CDeviceController {
  private final Mpu6050Sensor sensor;

  public Mpu6050Controller() { sensor = null; }

  protected Mpu6050Controller(AddressableDevice device) throws IOException {
    super(device);
    sensor = new Mpu6050Sensor(device);
  }

  @Override public String getName() { return "MPU6050"; }
  @Override public String getDescription() { return "MPU-6050 six-axis accelerometer and gyroscope"; }
  @Override public boolean canDetect() { return true; }
  @Override public I2CDevice getDevice() { return sensor; }
  @Override public DeviceType getType() { return DeviceType.SENSOR; }
  @Override public int[] getAddressRange() { return new int[]{0x68, 0x69}; }

  @Override
  public boolean detect(AddressableDevice device) {
    try {
      return (device.getDevice() == 0x68 || device.getDevice() == 0x69)
          && device.readRegister(Mpu6050Sensor.WHO_AM_I) == Mpu6050Sensor.CHIP_ID;
    } catch (Pi4JException _) {
      return false;
    }
  }

  @Override
  public I2CDeviceController mount(AddressableDevice device) throws IOException {
    synchronized (I2CDeviceScheduler.getI2cBusLock()) {
      return new Mpu6050Controller(device);
    }
  }

  @Override
  public byte[] getDeviceState() throws IOException {
    if (sensor == null) return super.getDeviceState();
    sensor.beginSample();
    try {
      return super.getDeviceState();
    } finally {
      sensor.endSample();
    }
  }

  @Override
  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig(buildSchema(sensor));
    config.setTitle(getName());
    config.setComments(getDescription());
    config.setVersion(1);
    config.setUniqueId(getSchemaId());
    config.setResourceType("sensor");
    config.setDescription("Acceleration in m/s², angular velocity in rad/s and internal temperature in °C; 100 Hz");
    return config;
  }
}
