/*
 *
 *  Copyright [ 2020 - 2024 ] Matthew Buckton
 *  Copyright [ 2024 - 2026 ] MapsMessaging B.V.
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;


public class BNO055Controller extends I2CDeviceController {
  private static final String SCHEMA_ACCELEROMETER = "accelerometer";
  private static final String SCHEMA_MAGNETOMETER = "magnetometer";
  private static final String SCHEMA_GYROSCOPE = "gyroscope";
  private static final String SCHEMA_OBJECT = "object";
  private static final String SCHEMA_PROPERTIES = "properties";

  private final int[] i2cAddr = {0x28, 0x29};
  private final BNO055Sensor sensor;

  public BNO055Controller() {
    sensor = null;
  }

  @Override
  public String getName() {
    return "BNO055";
  }

  @Override
  public String getDescription() {
    return "BNO055 orientation sensor";
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
    return (BNO055Sensor.getId(i2cDevice) == BNO055Constants.BNO055_ID);
  }

  public DeviceType getType() {
    return getDevice().getType();
  }

  public I2CDeviceController mount(AddressableDevice device) throws IOException {
    return new BNO055Controller(device);
  }

  @Override
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

  @Override
  public byte[] getDeviceConfiguration() throws IOException {
    JsonObject jsonObject = new JsonObject();
    if (sensor != null) {
      JsonObject calibrationStatus = new JsonObject();
      calibrationStatus.addProperty("isCalibrated", sensor.isSystemCalibration());
      calibrationStatus.addProperty(SCHEMA_ACCELEROMETER, sensor.getAccelerometerCalibration().name());
      calibrationStatus.addProperty(SCHEMA_MAGNETOMETER, sensor.getMagnetometerCalibration().name());
      calibrationStatus.addProperty("system", sensor.getSystemCalibration().name());
      calibrationStatus.addProperty(SCHEMA_GYROSCOPE, sensor.getGryoscopeCalibration().name());

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
    JsonObject state = new JsonObject();
    state.addProperty("type", SCHEMA_OBJECT);
    JsonObject fields = new JsonObject();
    for (String field : new String[] {"heading", "roll", "pitch"}) {
      JsonObject value = new JsonObject();
      value.addProperty("type", "number");
      value.addProperty("description", "Euler orientation angle in degrees");
      value.addProperty("readOnly", true);
      fields.add(field, value);
    }
    state.add(SCHEMA_PROPERTIES, fields);
    state.addProperty("additionalProperties", false);

    // The calibration report is returned by getDeviceConfiguration(), not getDeviceState().
    JsonObject configuration = new JsonObject();
    configuration.addProperty("type", SCHEMA_OBJECT);
    JsonObject configurationProperties = new JsonObject();
    JsonObject statuses = new JsonObject();
    statuses.addProperty("type", "array");
    JsonObject statusText = new JsonObject();
    statusText.addProperty("type", "string");
    statuses.add("items", statusText);
    configurationProperties.add("systemStatus", statuses);
    configurationProperties.add("errorStatus", statusText.deepCopy());
    JsonObject calibration = new JsonObject();
    calibration.addProperty("type", SCHEMA_OBJECT);
    JsonObject calibrationProperties = new JsonObject();
    JsonObject calibrated = new JsonObject();
    calibrated.addProperty("type", "boolean");
    calibrationProperties.add("isCalibrated", calibrated);
    JsonObject status = new JsonObject();
    status.addProperty("type", "string");
    JsonArray calibrationValues = new JsonArray();
    for (io.mapsmessaging.devices.i2c.devices.sensors.bno055.values.CalibrationStatus value :
        io.mapsmessaging.devices.i2c.devices.sensors.bno055.values.CalibrationStatus.values()) {
      calibrationValues.add(value.name());
    }
    status.add("enum", calibrationValues);
    for (String component : new String[] {SCHEMA_ACCELEROMETER, SCHEMA_MAGNETOMETER, "system", SCHEMA_GYROSCOPE}) {
      calibrationProperties.add(component, status.deepCopy());
    }
    calibration.add(SCHEMA_PROPERTIES, calibrationProperties);
    configurationProperties.add("calibrationStatus", calibration);
    JsonObject version = new JsonObject();
    version.addProperty("type", SCHEMA_OBJECT);
    JsonObject versionProperties = new JsonObject();
    JsonObject software = new JsonObject();
    software.addProperty("type", "number");
    versionProperties.add("software", software);
    JsonObject revision = new JsonObject();
    revision.addProperty("type", "integer");
    for (String component : new String[] {"bootloader", SCHEMA_ACCELEROMETER, SCHEMA_MAGNETOMETER, SCHEMA_GYROSCOPE}) {
      versionProperties.add(component, revision.deepCopy());
    }
    version.add(SCHEMA_PROPERTIES, versionProperties);
    configurationProperties.add("version", version);
    configuration.add(SCHEMA_PROPERTIES, configurationProperties);
    state.add("x-deviceConfiguration", configuration);

    JsonSchemaConfig config = new JsonSchemaConfig(gson.toJson(state));
    config.setComments("i2c device BNO055 orientation sensor");
    config.setTitle(getName());
    config.setVersion(1);
    config.setResourceType("sensor");
    config.setUniqueId(getSchemaId());
    config.setDescription("getDeviceState returns heading, roll and pitch; configuration returns calibration, status and version");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    return i2cAddr;
  }
}
