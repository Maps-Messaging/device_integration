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

package io.mapsmessaging.devices.spi;

import com.pi4j.context.Context;
import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.schemas.config.SchemaConfig;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class SpiDeviceScheduler extends SpiDeviceController {

  private static final Semaphore SPI_BUS_SEMAPHORE = new Semaphore(1);

  private final SpiDeviceController deviceController;

  public SpiDeviceScheduler(SpiDeviceController deviceController) {
    this.deviceController = deviceController;
  }

  @Override
  public String getName() {
    return deviceController.getName();
  }

  @Override
  public String getDescription() {
    return deviceController.getDescription();
  }

  @Override
  public SchemaConfig getSchema() {
    return deviceController.getSchema();
  }

  @Override
  public byte[] getDeviceConfiguration() throws IOException {
    try {
      SPI_BUS_SEMAPHORE.acquireUninterruptibly();
      return deviceController.getDeviceConfiguration();
    } finally {
      SPI_BUS_SEMAPHORE.release();
    }
  }

  public DeviceType getType() {
    return deviceController.getType();
  }

  @Override
  public byte[] getDeviceState() throws IOException {
    try {
      SPI_BUS_SEMAPHORE.acquireUninterruptibly();
      return deviceController.getDeviceState();
    } finally {
      SPI_BUS_SEMAPHORE.release();
    }
  }

  @Override
  public byte[] updateDeviceConfiguration(byte[] val) throws IOException {
    try {
      SPI_BUS_SEMAPHORE.acquireUninterruptibly();
      return deviceController.updateDeviceConfiguration(val);
    } finally {
      SPI_BUS_SEMAPHORE.release();
    }
  }

  @Override
  public SpiDeviceController mount(Context pi4j, Map<String, String> config) {
    return null; // Device is already mounted
  }
}