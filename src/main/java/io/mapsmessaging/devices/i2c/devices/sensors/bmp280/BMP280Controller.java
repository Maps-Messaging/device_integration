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

package io.mapsmessaging.devices.i2c.devices.sensors.bmp280;

import com.google.gson.JsonObject;
import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.I2CDeviceScheduler;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BMP280Controller extends I2CDeviceController {

  private static final int i2cAddr = 0x78;
  private final BMP280Sensor sensor;

  public BMP280Controller() {
    sensor = null;
  }

  @Override
  public String getName() {
    return "BMP280";
  }

  @Override
  public String getDescription() {
    return "Pressure and Temperature Module";
  }

  protected BMP280Controller(AddressableDevice device) throws IOException {
    super(device);
    sensor = new BMP280Sensor(device);
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
      return new BMP280Controller(device);
    }
  }

  @Override
  public byte[] getDeviceConfiguration() {
    return "{}".getBytes();
  }

  public DeviceType getType() {
    return getDevice().getType();
  }

  @Override
  public byte[] getDeviceState() throws IOException {
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("pressure", sensor.getPressure());
    jsonObject.addProperty("temperature", sensor.getTemperature());
    return gson.toJson(jsonObject).getBytes(StandardCharsets.UTF_8);
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig(buildSchema(sensor));
    config.setComments("i2c device BMP280 Pressure and Temperature Sensor https://www.bosch-sensortec.com/products/environmental-sensors/pressure-sensors/bmp280/");
    config.setTitle(getName());
    config.setVersion(1);
    config.setResourceType("sensor");
    config.setUniqueId(getSchemaId());
    config.setInterfaceDescription("Returns JSON object containing Temperature and Pressure");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    return new int[]{i2cAddr};
  }

}
