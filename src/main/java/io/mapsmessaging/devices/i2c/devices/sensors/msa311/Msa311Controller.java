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

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import lombok.Getter;
import org.json.JSONObject;

import java.io.IOException;

public class Msa311Controller extends I2CDeviceController {

  private final int i2cAddr = 0x62;
  private final Msa311Sensor sensor;

  @Getter
  private final String name = "MSA311";

  @Getter
  private final String description = "Digital Tri-axial Accelerometer";

  public Msa311Controller() {
    sensor = null;
  }

  public Msa311Controller(I2C device) {
    super(device);
    sensor = new Msa311Sensor(device);
  }

  @Override
  public boolean detect(I2C i2cDevice) {
    return sensor != null && sensor.isConnected();
  }

  public I2CDeviceController mount(I2C device) {
    return new Msa311Controller(device);
  }

  public byte[] getStaticPayload() throws IOException {
    JSONObject jsonObject = new JSONObject();
    if (sensor != null) {
    }
    return jsonObject.toString(2).getBytes();
  }

  public JSONObject pack() {
    JSONObject jsonObject = new JSONObject();

    return jsonObject;
  }

  public byte[] getUpdatePayload() throws IOException {
    JSONObject jsonObject = new JSONObject();
    if (sensor != null) {
    }
    return jsonObject.toString(2).getBytes();
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig();
    config.setComments("Digital Tri-axial Accelerometer");
    config.setSource("I2C bus address : " + i2cAddr);
    config.setVersion("1.0");
    config.setResourceType("sensor");
    config.setInterfaceDescription("Digital Tri-axial Accelerometer");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    return new int[]{i2cAddr};
  }
}