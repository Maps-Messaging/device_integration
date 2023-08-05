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

public class ServoDemo {

  private static final int SERVO_PWM_FREQUENCY = 60;
  private static final int SERVO_COUNT = 16;

  private static final int SERVO_LOWER_BOUND = 150;
  private static final int SERVO_HIGHER_BOUND = 550;

  public ServoDemo(Pca9685Device device) throws IOException {
    synchronized (I2CDeviceScheduler.getI2cBusLock()) {
      device.reset();
      device.setPWMFrequency(SERVO_PWM_FREQUENCY);
      // Allocate the servos. This code simple manages the bounds that the servo can work within

      Servo[] servos = new Servo[SERVO_COUNT];
      for (int x = 0; x < servos.length; x++) {
        servos[x] = new Servo(device, x, new LinearResponse(SERVO_LOWER_BOUND, SERVO_HIGHER_BOUND, 0f, 360f));
      }

      float pos = 0.0f;
      while(pos < 360) {
        servos[1].setPosition(pos);
        servos[4].setPosition(pos);
        device.delay(1000);
        pos += 10f;
      }
      while(pos > 0.1f){
        servos[1].setPosition(pos);
        servos[4].setPosition(pos);
        device.delay(1000);
        pos -= 10f;
      }
    }
  }

  private void rotate(Servo servo) throws IOException {
    for (int x = 0; x < 360; x += 10) {
      servo.setPosition(x);
      servo.myPWMController.delay(50);
    }
    for (int x = 360; x > 0; x -= 10) {
      servo.setPosition(x);
      servo.myPWMController.delay(50);
    }
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    I2CBusManager[] i2cBusManagers = DeviceBusManager.getInstance().getI2cBusManager();
    int bus = 1;
    if (args.length > 0) {
      bus = Integer.parseInt(args[0]);
    }

    // Configure and mount a device on address 0x5D as a LPS25 pressure & temperature
    I2CDeviceController deviceController = i2cBusManagers[bus].configureDevice(0x40, "PCA9685");
    if (deviceController != null) {
      I2CDevice sensor = deviceController.getDevice();
      if (sensor instanceof Pca9685Device) {
        new ServoDemo((Pca9685Device) sensor);
      }
    }

  }
}
