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

package io.mapsmessaging.devices.i2c.devices.rtc.ds3231;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;

import java.io.IOException;

public class Ds3231Controller extends I2CDeviceController {

  private static final int i2cAddr = 0x68;
  private final Ds3231Rtc rtc;

  public Ds3231Controller() {
    rtc = null;
  }

  @Override
  public String getName() {
    return "DS3231";
  }

  @Override
  public String getDescription() {
    return "Real Time Clock with temperature calibration";
  }

  public Ds3231Controller(AddressableDevice device) throws IOException {
    rtc = new Ds3231Rtc(device);
  }

  public I2CDevice getDevice() {
    return rtc;
  }

  @Override
  public boolean canDetect() {
    return true;
  }

  public DeviceType getType() {
    return getDevice().getType();
  }

  @Override
  public boolean detect(AddressableDevice i2cDevice) {
    try {
      return Ds3231Rtc.detect(i2cDevice);
    } catch (IOException e) {

    }
    return false;
  }

  public I2CDeviceController mount(AddressableDevice device) throws IOException {
    return new Ds3231Controller(device);
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig(buildSchema(rtc));
    config.setComments("i2c RTC");
    config.setTitle(getName());
    config.setVersion(1);
    config.setUniqueId(getSchemaId());
    config.setResourceType("rtc");
    config.setInterfaceDescription("Returns JSON object containing the latest rtc");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    return new int[]{i2cAddr};
  }

}