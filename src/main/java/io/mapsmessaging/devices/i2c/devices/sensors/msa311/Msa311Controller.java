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

package io.mapsmessaging.devices.i2c.devices.sensors.msa311;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.I2CDeviceScheduler;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import lombok.Getter;

import java.io.IOException;

public class Msa311Controller extends I2CDeviceController {

  private final int i2cAddr = 0x63;
  private final Msa311Sensor sensor;

  @Getter
  private final String name = "MSA311";

  @Getter
  private final String description = "Digital Tri-axial Accelerometer";

  public Msa311Controller() {
    sensor = null;
  }

  public Msa311Controller(AddressableDevice device) throws IOException {
    super(device);
    synchronized (I2CDeviceScheduler.getI2cBusLock()) {
      sensor = new Msa311Sensor(device);
    }
  }

  public I2CDevice getDevice() {
    return sensor;
  }
  public DeviceType getType(){
    return getDevice().getType();
  }


  @Override
  public boolean canDetect() {
    return true;
  }

  @Override
  public boolean detect(AddressableDevice i2cDevice) {
    return (Msa311Sensor.getId(i2cDevice) == 0b00010011);
  }

  public I2CDeviceController mount(AddressableDevice device) throws IOException {
    return new Msa311Controller(device);
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig();
    config.setComments(description);
    config.setSource(getName());
    config.setVersion("1.0");
    config.setResourceType("sensor");
    config.setInterfaceDescription(description);
    return config;
  }

  @Override
  public int[] getAddressRange() {
    return new int[]{i2cAddr};
  }
}