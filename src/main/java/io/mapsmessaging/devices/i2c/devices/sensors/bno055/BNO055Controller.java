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

package io.mapsmessaging.devices.i2c.devices.sensors.bno055;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.I2CDeviceScheduler;
import io.mapsmessaging.devices.i2c.devices.sensors.bno055.values.SystemStatus;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.devices.sensorreadings.Orientation;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import lombok.Getter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


public class BNO055Controller extends I2CDeviceController {

  private final int[] i2cAddr = {0x28, 0x29};
  private final BNO055Sensor sensor;
  @Getter
  private final String name = "BNO055";
  @Getter
  private final String description = "BNO055 orientation sensor";

  public BNO055Controller() {
    sensor = null;
  }

  protected BNO055Controller(AddressableDevice device) throws IOException {
    super(device);
    synchronized (I2CDeviceScheduler.getI2cBusLock()) {
      sensor = new BNO055Sensor(device);
    }
  }

  public I2CDevice getDevice() {
    return sensor;
  }

  @Override
  public boolean canDetect() {
    return true;
  }

  @Override
  public boolean detect(AddressableDevice i2cDevice) {
    return (BNO055Sensor.getId(i2cDevice) == BNO055Constants.BNO055_CHIP_ID_ADDR);
  }

  public DeviceType getType() {
    return getDevice().getType();
  }

  public I2CDeviceController mount(AddressableDevice device) throws IOException {
    return new BNO055Controller(device);
  }

  public byte[] getDeviceState() throws IOException {
    JsonObject jsonObject = new JsonObject();
    if (sensor != null) {
      Orientation orientation = sensor.getOrientation();
      jsonObject.addProperty("heading", orientation.getX());
      jsonObject.addProperty("roll", orientation.getY());
      jsonObject.addProperty("pitch", orientation.getZ());
    }
    return gson.toJson(jsonObject).getBytes(StandardCharsets.UTF_8);
  }

  public byte[] getDeviceConfiguration() throws IOException {
    JsonObject jsonObject = new JsonObject();
    if (sensor != null) {
      JsonObject calibrationStatus = new JsonObject();
      calibrationStatus.addProperty("isCalibrated", sensor.isSystemCalibration());
      calibrationStatus.addProperty("accelerometer", sensor.getAccelerometerCalibration().name());
      calibrationStatus.addProperty("magnetometer", sensor.getMagnetometerCalibration().name());
      calibrationStatus.addProperty("system", sensor.getSystemCalibration().name());
      calibrationStatus.addProperty("gyroscope", sensor.getGryoscopeCalibration().name());

      JsonArray statusArray = new JsonArray();
      for (SystemStatus status : sensor.getSystemStatusRegister().getStatus()) {
        statusArray.add(status.getDescription());
      }

      jsonObject.add("systemStatus", statusArray);
      jsonObject.addProperty("errorStatus", sensor.getErrorStatus().getDescription());

      JsonObject version = gson.toJsonTree(sensor.getVersion()).getAsJsonObject();
      jsonObject.add("version", version);

      jsonObject.add("calibrationStatus", calibrationStatus);
    }
    return gson.toJson(jsonObject).getBytes(StandardCharsets.UTF_8);
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig(buildSchema(sensor));
    config.setComments("i2c device BNO055 orientation sensor");
    config.setTitle(getName());
    config.setVersion(1);
    config.setResourceType("sensor");
    config.setUniqueId(getSchemaId());
    config.setInterfaceDescription("Returns JSON object containing Temperature and Pressure");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    return i2cAddr;
  }
}
