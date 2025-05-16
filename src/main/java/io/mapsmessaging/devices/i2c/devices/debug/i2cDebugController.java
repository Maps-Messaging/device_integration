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

package io.mapsmessaging.devices.i2c.devices.debug;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.I2CDeviceScheduler;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

public class i2cDebugController extends I2CDeviceController {

  private static final int I2C_ADDRESS = 0x0;
  private static final String NAME = "DebugDevice";
  private static final String DESCRIPTION = "i2c Debug Device, simply updates the time every second";

  private final i2cDebugDevice device;

  @Getter
  @Setter
  private boolean throwErrror;

  public i2cDebugController() {
    device = null;
  }

  public i2cDebugController(AddressableDevice device) {
    super(device);
    synchronized (I2CDeviceScheduler.getI2cBusLock()) {
      this.device = new i2cDebugDevice(device);
    }
    throwErrror = false;
  }

  public I2CDevice getDevice() {
    return device;
  }

  public DeviceType getType() {
    return device.getType();
  }

  public I2CDeviceController mount(AddressableDevice device) throws IOException {
    return new i2cDebugController(device);
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
    JsonSchemaConfig config = new JsonSchemaConfig(buildSchema(device));
    config.setComments(DESCRIPTION);
    config.setSource(getName());
    config.setVersion("1.0");
    config.setUniqueId(getSchemaId());
    config.setResourceType("sensor");
    config.setInterfaceDescription("Debug device, updates time");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    return new int[]{I2C_ADDRESS};
  }

  @Override
  public byte[] getDeviceState() throws IOException {
    if (throwErrror) {
      throwErrror = false;
      throw new IOException("Exception requested on next read");
    }
    return super.getDeviceState();
  }
}