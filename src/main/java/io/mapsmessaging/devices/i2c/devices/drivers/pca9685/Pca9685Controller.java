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

import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.I2CDeviceScheduler;
import io.mapsmessaging.devices.i2c.devices.drivers.pca9685.registers.Mode1Register;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.schemas.config.SchemaConfig;
import io.mapsmessaging.schemas.config.impl.JsonSchemaConfig;
import lombok.Getter;

import java.io.IOException;

public class Pca9685Controller extends I2CDeviceController {

  private static final int i2cAddr = 0x40;
  private final Pca9685Device device;

  @Getter
  private final String name = "PCA9685";

  @Getter
  private final String description = "i2c device PCA9685 supports 16 PWM devices like servos or LEDs";

  public Pca9685Controller() {
    device = null;
  }

  public Pca9685Controller(AddressableDevice device) throws IOException {
    super(device);
    synchronized (I2CDeviceScheduler.getI2cBusLock()) {
      this.device = new Pca9685Device(device);
      this.device.setPWMFrequency(60);
      this.device.getMode1Register().setRespondToAddr(Mode1Register.ADDRESS_1|Mode1Register.ADDRESS_3);
      int val[] = new int[2];
      val[0] = 0x777;
      val[1] = 0x1;
      for(int x=0;x<10;x++) {
        int off = val[x%2];
        int on = val[(x+1)%2];
        this.device.setPWM(0, on, off);
        this.device.delay(100);
        this.device.setPWM(1, on, off);
        this.device.delay(100);
        this.device.setPWM(2,on, off);
        this.device.delay(100);
        this.device.setPWM(3, on, off);
        this.device.delay(100);
        this.device.setPWM(4, on, off);
        this.device.delay(100);
        this.device.setPWM(5, on, off);
        this.device.delay(100);
        this.device.setPWM(6, on, off);
        this.device.delay(100);
        this.device.setPWM(7, on, off);
        this.device.delay(100);
      }
    }
  }

  public I2CDevice getDevice() {
    return device;
  }


  public I2CDeviceController mount(AddressableDevice device) throws IOException {
    return new Pca9685Controller(device);
  }

  @Override
  public boolean detect(AddressableDevice i2cDevice) {
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