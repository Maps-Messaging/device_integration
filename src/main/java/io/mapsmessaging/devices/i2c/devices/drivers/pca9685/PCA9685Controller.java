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

package io.mapsmessaging.devices.i2c.devices.drivers.pca9685;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.devices.drivers.pca9685.servos.Servo;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import lombok.Getter;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PCA9685Controller extends I2CDeviceController {

  private final int i2cAddr = 0x40;
  private final PCA9685Device device;
  private final List<Servo> connected = new ArrayList<>();

  @Getter
  private final String name = "PCA9685";

  public PCA9685Controller() {
    device = null;
  }

  public PCA9685Controller(I2C device) throws IOException {
    super(device);
    this.device = new PCA9685Device(device);
    this.device.setPWMFrequency(60);
  }


  public I2CDeviceController mount(I2C device) throws IOException {
    return new PCA9685Controller(device);
  }

  public byte[] getStaticPayload() {
    JSONObject jsonObject = new JSONObject();
    return jsonObject.toString(2).getBytes();
  }

  public byte[] getUpdatePayload() {
    JSONObject jsonObject = new JSONObject();
    return jsonObject.toString(2).getBytes();
  }

  @Override
  public byte[] setPayload(byte[] val) {
    return "{}".getBytes();
  }

  @Override
  public boolean detect() {
    return device != null && device.isConnected();
  }

  public SchemaConfig getSchema() {
    JsonSchemaConfig config = new JsonSchemaConfig();
    config.setComments("i2c device PCA9685 supports 16 PWM devices like servos or LEDs");
    config.setSource("I2C bus address : 0x40");
    config.setVersion("1.0");
    config.setResourceType("driver");
    config.setInterfaceDescription("Manages the output of 16 PWM devices");
    return config;
  }

  @Override
  public int[] getAddressRange() {
    return new int[]{i2cAddr};
  }
}