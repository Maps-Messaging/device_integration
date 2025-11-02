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

package io.mapsmessaging.devices.i2c;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.schemas.model.XRegistrySchemaVersion;
import lombok.Getter;

import java.io.IOException;

/**
 * The locking here basically disables multiple access to the same device and will limit the
 * access to the I2C bus. If a device calls delay, then another device can take ownership of the bus
 * and perform any operation required. The device that has called delay will need to wait for the
 * new operation on the I2C bus to complete.
 */
public class I2CDeviceScheduler extends I2CDeviceController {

  private static final Object I2C_BUS_LOCK = new Object();
  @Getter
  private final I2CDeviceController deviceController;

  public I2CDeviceScheduler(I2CDeviceController deviceController) {
    this.deviceController = deviceController;
  }

  public static Object getI2cBusLock() {
    return I2C_BUS_LOCK;
  }

  @Override
  public boolean getRaiseExceptionOnError() {
    return deviceController.getRaiseExceptionOnError();
  }

  @Override
  public void setRaiseExceptionOnError(boolean flag) {
    deviceController.setRaiseExceptionOnError(flag);
  }

  @Override
  public int getMountedAddress() {
    return deviceController.getMountedAddress();
  }

  @Override
  public String getName() {
    return deviceController.getName();
  }

  public I2CDevice getDevice() {
    return deviceController.getDevice();
  }

  public DeviceType getType() {
    return getDevice().getType();
  }

  @Override
  public String getDescription() {
    return deviceController.getDescription();
  }

  @Override
  public XRegistrySchemaVersion getSchema() {
    return deviceController.getSchema();
  }

  @Override
  public byte[] getDeviceConfiguration() throws IOException {
    synchronized (deviceController) {
      synchronized (I2C_BUS_LOCK) {
        return deviceController.getDeviceConfiguration();
      }
    }
  }

  @Override
  public byte[] getDeviceState() throws IOException {
    synchronized (deviceController) {
      synchronized (I2C_BUS_LOCK) {
        return deviceController.getDeviceState();
      }
    }
  }

  @Override
  public byte[] updateDeviceConfiguration(byte[] val) throws IOException {
    synchronized (deviceController) {
      synchronized (I2C_BUS_LOCK) {
        return deviceController.updateDeviceConfiguration(val);
      }
    }
  }

  @Override
  public I2CDeviceController mount(AddressableDevice device) throws IOException {
    throw new IOException("Device already mounted");
  }

  @Override
  public int[] getAddressRange() {
    return deviceController.getAddressRange();
  }

  @Override
  public boolean detect(AddressableDevice i2cDevice) {
    return true; // This is indeed a physical device
  }
}
