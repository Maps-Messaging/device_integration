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

package io.mapsmessaging.devices.pwm.servo;

import io.mapsmessaging.devices.DeviceBusManager;
import io.mapsmessaging.devices.i2c.I2CBusManager;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.I2CDeviceScheduler;
import io.mapsmessaging.devices.i2c.devices.drivers.pca9685.Pca9685Device;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class Servos {

  private static final int SERVO_PWM_FREQUENCY = 60;
  private static final int SERVO_COUNT = 16;

  private static final int SERVO_LOWER_BOUND = 110;
  private static final int SERVO_HIGHER_BOUND = 590;
  public Servos(Pca9685Device device) throws IOException {
    synchronized (I2CDeviceScheduler.getI2cBusLock()) {
      device.setPWMFrequency(SERVO_PWM_FREQUENCY);
      // Allocate the servos. This code simple manages the bounds that the servo can work within

      Servo[] servos = new Servo[SERVO_COUNT];
      for (int x = 0; x < servos.length; x++) {
        servos[x] = new Servo(device, x, new LinearResponse(SERVO_LOWER_BOUND, SERVO_HIGHER_BOUND, 0f, 360f));
      }

      for (Servo servo : servos) {
        rotate(servo);
      }
    }
  }

  private void rotate(Servo servo) throws IOException {
    for (int x = 0; x < 360; x += 10) {
      servo.setPosition(x);
      servo.myPWMController.delay(10);
    }
    for (int x = 360; x > 0; x -= 10) {
      servo.setPosition(x);
      servo.myPWMController.delay(10);
    }
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    I2CBusManager[] i2cBusManagers = DeviceBusManager.getInstance().getI2cBusManager();
    int bus = 1;
    if (args.length > 0) {
      bus = Integer.parseInt(args[0]);
    }

    // Configure and mount a device on address 0x5D as a LPS25 pressure & temperature
    Map<String, Object> map = new LinkedHashMap<>();
    Map<String, Object> config = new LinkedHashMap<>();
    config.put("deviceName", "PCA9685");
    map.put(""+0x40, config);
    I2CDeviceController deviceController = i2cBusManagers[bus].configureDevices(map);
    if (deviceController != null) {
      I2CDevice sensor = deviceController.getDevice();
      if (sensor instanceof Pca9685Device) {
        new Servos((Pca9685Device) sensor);
      }
    }

  }
}
