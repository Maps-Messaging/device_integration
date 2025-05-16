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

package io.mapsmessaging.devices.pmsa03;

import io.mapsmessaging.devices.DeviceBusManager;
import io.mapsmessaging.devices.i2c.I2CBusManager;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.devices.sensors.pmsa003i.Pmsa003iSensor;
import lombok.SneakyThrows;

import java.io.IOException;

public class Pmsa003Publisher implements Runnable {

  private final I2CDeviceController deviceController;

  public Pmsa003Publisher(I2CDeviceController deviceController) {
    this.deviceController = deviceController;
    Thread t = new Thread(this);
    t.start();
  }

  public static void main(String[] args) throws IOException {
    I2CBusManager[] i2cBusManagers = DeviceBusManager.getInstance().getI2cBusManager();
    int bus = 1;
    if (args.length > 0) {
      bus = Integer.parseInt(args[0]);
    }
    // Configure and mount a device on address 0x5D as a LPS25 pressure & temperature
    I2CDeviceController deviceController = i2cBusManagers[bus].configureDevice(0x12, "PMSA003I");
    if (deviceController != null) {
      I2CDevice sensor = deviceController.getDevice();
      if (sensor instanceof Pmsa003iSensor) {
        new Pmsa003Publisher(deviceController);
      }
    }
  }

  @SneakyThrows
  public void run() {
    deviceController.setRaiseExceptionOnError(true);
    for(int x=0;x<100000;x++){
      System.err.println(new String(deviceController.getDeviceState()));
      Thread.sleep(3000);
    }
  }
}
