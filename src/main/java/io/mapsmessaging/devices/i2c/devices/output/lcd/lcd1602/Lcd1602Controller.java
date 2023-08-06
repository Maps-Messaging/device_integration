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

package io.mapsmessaging.devices.i2c.devices.output.lcd.lcd1602;

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import lombok.Getter;
import org.json.JSONObject;

import java.io.IOException;

public class Lcd1602Controller extends I2CDeviceController {

  private final Lcd1602Device display;

  @Getter
  private final String name = "LCD1602";
  @Getter
  private final String description = "LCD1602 16*2 lcd display";

  // Used during ServiceLoading
  public Lcd1602Controller() {
    display = null;
  }

  protected Lcd1602Controller(AddressableDevice device) throws IOException {
    super(device);
    display = new Lcd1602Device(device);
  }

  public I2CDevice getDevice() {
    return display;
  }

  @Override
  public boolean detect(AddressableDevice i2cDevice) {
    return display != null && display.isConnected();
  }

  public I2CDeviceController mount(AddressableDevice device) throws IOException {
    return new Lcd1602Controller(device);
  }

  public byte[] getDeviceConfiguration() throws IOException {
    JSONObject jsonObject = new JSONObject();
    if (display != null) {
    }
    return jsonObject.toString(2).getBytes();
  }

  public byte[] getDeviceState() throws IOException {
    JSONObject jsonObject = new JSONObject();
    if (display != null) {
    }
    return jsonObject.toString(2).getBytes();
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig();
    config.setComments("i2c device AM2315 encased Temperature and Humidity Sensor https://www.adafruit.com/product/1293");
    config.setSource("I2C bus address : 0x5C");
    config.setVersion("1.0");
    config.setResourceType("sensor");
    config.setInterfaceDescription("temperature, humidity");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    return new int[]{0X7C};
  }
}
