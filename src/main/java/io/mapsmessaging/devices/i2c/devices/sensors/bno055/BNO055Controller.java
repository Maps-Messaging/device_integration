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
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;


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
    JSONObject jsonObject = new JSONObject();
    if (sensor != null) {
      Orientation orientation = sensor.getOrientation();
      jsonObject.put("heading", orientation.getX());
      jsonObject.put("roll", orientation.getY());
      jsonObject.put("pitch", orientation.getZ());
    }
    return jsonObject.toString(2).getBytes();
  }

  public byte[] getDeviceConfiguration() throws IOException {
    JSONObject jsonObject = new JSONObject();
    if (sensor != null) {
      JSONObject callibrationStatus = new JSONObject();
      callibrationStatus.put("isCalibrated", sensor.isSystemCalibration());
      callibrationStatus.put("accelerometer", sensor.getAccelerometerCalibration().name());
      callibrationStatus.put("magnetometer", sensor.getMagnetometerCalibration().name());
      callibrationStatus.put("system", sensor.getSystemCalibration().name());
      callibrationStatus.put("gyroscope", sensor.getGryoscopeCalibration().name());
      JSONArray statusArray = new JSONArray();
      for (SystemStatus status : sensor.getSystemStatusRegister().getStatus()) {
        statusArray.put(status.getDescription());
      }
      jsonObject.put("systemStatus", statusArray);
      jsonObject.put("errorStatus", sensor.getErrorStatus().getDescription());
      jsonObject.put("version", new JSONObject(sensor.getVersion()));
      jsonObject.put("calibrationStatus", callibrationStatus);
    }
    return jsonObject.toString(2).getBytes();
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig(buildSchema(sensor));
    config.setComments("i2c device BNO055 orientation sensor");
    config.setSource(getName());
    config.setVersion("1.0");
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
