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

package io.mapsmessaging.devices.i2c.devices.output.lcd.lcd1602.backlight;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;

import java.io.IOException;

public abstract class BacklightPwmController extends I2CDeviceController {

  protected final BacklightPwm pwmController;

  // Used during ServiceLoading
  protected BacklightPwmController() {
    pwmController = null;
  }

  protected BacklightPwmController(AddressableDevice device, BacklightPwm pwmController) {
    super(device);
    this.pwmController = pwmController;
  }

  public DeviceType getType() {
    return pwmController.getType();
  }

  public I2CDevice getDevice() {
    return pwmController;
  }

  @Override
  public String getName() {
    return "PwmController";
  }

  @Override
  public String getDescription() {
    return "LCD1602 16*2 lcd display";
  }

  @Override
  public boolean detect(AddressableDevice i2cDevice) {
    return pwmController != null && pwmController.isConnected();
  }

  @Override
  public byte[] getDeviceConfiguration() throws IOException {
    return emptyJson();
  }

  @Override
  public byte[] getDeviceState() throws IOException {
    return emptyJson();
  }


  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig();
    config.setComments("i2c device AM2315 encased Temperature and Humidity Sensor https://www.adafruit.com/product/1293");
    config.setSource("I2C bus address : 0x5C");
    config.setVersion(1);
    config.setResourceType("sensor");
    config.setInterfaceDescription("temperature, humidity");
    return config;
  }
}
