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

package io.mapsmessaging.devices.i2c.devices.sensors.pmsa003i;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import lombok.Getter;
import org.json.JSONObject;

import java.io.IOException;

public class Pmsa003iController extends I2CDeviceController {

  private final int i2cAddr = 0x12;
  private final Pmsa003iSensor sensor;

  @Getter
  private final String name = "PMSA003I";

  @Getter
  private final String description = "Air Quality sensor";

  public Pmsa003iController() {
    sensor = null;
  }

  public Pmsa003iController(I2C device) {
    super(device);
    sensor = new Pmsa003iSensor(device);
  }

  @Override
  public boolean detect(I2C i2cDevice) {
    return sensor != null && sensor.isConnected();
  }

  public I2CDeviceController mount(I2C device) {
    return new Pmsa003iController(device);
  }

  public byte[] getStaticPayload() throws IOException {
    JSONObject jsonObject = new JSONObject();
    if (sensor != null) {
      jsonObject.put("version", sensor.getVersion());
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
      jsonObject.put("Pm1_0_standard", sensor.getPm1_0Standard());
      jsonObject.put("Pm2_5_standard", sensor.getPm2_5Standard());
      jsonObject.put("Pm10_standard", sensor.getPm10Standard());
      jsonObject.put("Pm1_0_atmospheric", sensor.getPm1_0Atmospheric());
      jsonObject.put("Pm2_5_atmospheric", sensor.getPm2_5Atmospheric());
      jsonObject.put("Pm10_atmospheric", sensor.getPm10Atmospheric());
      jsonObject.put("particles_larger_than_0.3", sensor.getParticlesLargerThan3());
      jsonObject.put("particles_larger_than_0.5", sensor.getParticlesLargerThan5());
      jsonObject.put("particles_larger_than_1.0", sensor.getParticlesLargerThan10());
      jsonObject.put("particles_larger_than_2.5", sensor.getParticlesLargerThan25());
      jsonObject.put("particles_larger_than_5.0", sensor.getParticlesLargerThan50());
      jsonObject.put("particles_larger_than_10.0", sensor.getParticlesLargerThan100());
      jsonObject.put("error_code", sensor.getErrorCode());
    }
    return jsonObject.toString(2).getBytes();
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig();
    config.setComments("Air Quality Breakout");
    config.setSource("I2C bus address : " + i2cAddr);
    config.setVersion("1.0");
    config.setResourceType("sensor");
    config.setInterfaceDescription("Air Quality Breakout");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    return new int[]{i2cAddr};
  }
}