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

package io.mapsmessaging.devices.i2c.devices.sensors.bno055;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import lombok.Getter;
import org.json.JSONObject;

import java.io.IOException;


public class BNO055Controller extends I2CDeviceController {

  private final int[] i2cAddr = {0x28, 0x29};
  private final BNO055Sensor sensor;
  @Getter
  private final String name = "BNO055";

  public BNO055Controller() {
    sensor = null;
  }

  protected BNO055Controller(I2C device) throws IOException {
    super(device);
    sensor = new BNO055Sensor(device);
  }


  @Override
  public boolean detect() {
    return sensor != null && sensor.isConnected();
  }

  public I2CDeviceController mount(I2C device) throws IOException {
    return new BNO055Controller(device);
  }

  public byte[] getUpdatePayload() {
    JSONObject jsonObject = new JSONObject();
    if (sensor != null) {
      double[] orientation = sensor.getOrientation();
      jsonObject.put("heading", orientation[0]);
      jsonObject.put("roll", orientation[1]);
      jsonObject.put("pitch", orientation[2]);
    }
    return jsonObject.toString(2).getBytes();
  }

  public byte[] getStaticPayload() throws IOException {
    JSONObject jsonObject = new JSONObject();
    if (sensor != null) {
      CalibrationStatus status = sensor.getCalibrationStatus();
      JSONObject callibrationStatus = new JSONObject();
      callibrationStatus.put("accelerometer", status.getAccelerometer());
      callibrationStatus.put("magnetometer", status.getMagnetometer());
      callibrationStatus.put("system", status.getSystem());
      callibrationStatus.put("gyroscope", status.getGryoscope());
      SystemStatus system = sensor.getStatus(false);
      JSONObject systemStatus = new JSONObject();
      systemStatus.put("error", system.getErrorString());
      systemStatus.put("state", system.getStateString());
      systemStatus.put("selfTestGyroscope", system.selfTestGyroscope());
      systemStatus.put("selfTestMagnetometer", system.selfTestMagnetometer());
      systemStatus.put("selfTestAccelerometer", system.selfTestAccelerometer());
      systemStatus.put("selfTestMCU", system.selfTestMCU());
      jsonObject.put("isCalibrated", status.isCalibrated());
      jsonObject.put("version", new JSONObject(sensor.getVersion()));
      jsonObject.put("calibrationStatus", callibrationStatus);
      jsonObject.put("systemStatus", systemStatus);
    }
    return jsonObject.toString(2).getBytes();
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig();
    config.setComments("i2c device BNO055 orientation sensor");
    config.setSource("I2C bus address : 0x28");
    config.setVersion("1.0");
    config.setResourceType("sensor");
    config.setInterfaceDescription("Returns JSON object containing Temperature and Pressure");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    return i2cAddr;
  }
}
