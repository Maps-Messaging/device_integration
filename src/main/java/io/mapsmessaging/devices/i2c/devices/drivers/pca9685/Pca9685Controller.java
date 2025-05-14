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

package io.mapsmessaging.devices.i2c.devices.drivers.pca9685;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.I2CDeviceScheduler;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.devices.util.UuidGenerator;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;

import java.io.IOException;

public class Pca9685Controller extends I2CDeviceController {

  private static final int I2C_ADDRESS = 0x40;
  private static final String NAME = "PCA9685";
  private static final String DESCRIPTION = "i2c device PCA9685 supports 16 PWM devices like servos or LEDs";

  private final Pca9685Device device;

  public Pca9685Controller() {
    device = null;
  }

  public Pca9685Controller(AddressableDevice device) throws IOException {
    super(device);
    synchronized (I2CDeviceScheduler.getI2cBusLock()) {
      this.device = new Pca9685Device(device);
    }
  }

  public I2CDevice getDevice() {
    return device;
  }

  public DeviceType getType() {
    return device.getType();
  }

  public I2CDeviceController mount(AddressableDevice device) throws IOException {
    return new Pca9685Controller(device);
  }

  @Override
  public boolean detect(AddressableDevice i2cDevice) {
    return device != null && device.isConnected();
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String getDescription() {
    return DESCRIPTION;
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig();
    config.setComments(DESCRIPTION);
    config.setUniqueId(UuidGenerator.getInstance().generateUuid(getName()));
    config.setSource(getName());
    config.setVersion("1.0");
    config.setResourceType("driver");
    config.setInterfaceDescription("Manages the output of 16 PWM devices");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    return new int[]{I2C_ADDRESS};
  }
}