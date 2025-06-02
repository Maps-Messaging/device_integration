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

package io.mapsmessaging.devices.i2cmock;

import io.mapsmessaging.devices.i2c.I2CBusManager;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.I2CDeviceScheduler;
import io.mapsmessaging.devices.i2c.devices.demo.I2cDemoController;
import io.mapsmessaging.devices.impl.I2CDeviceImpl;
import io.mapsmessaging.devices.impl.I2CMockDeviceImpl;
import io.mapsmessaging.devices.logging.DeviceLogMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;


public class I2CMockBusManager extends I2CBusManager {


  public I2CMockBusManager(int bus) {
    super(bus);
    ServiceLoader<I2CDeviceController> deviceEntries = ServiceLoader.load(I2CDeviceController.class);

    for (I2CDeviceController device : deviceEntries) {
      if(device instanceof I2cDemoController) {
        knownDevices.put(device.getName(), device);
        logger.log(DeviceLogMessage.I2C_BUS_LOADED_DEVICE, device.getName());
        int[] addressRange = device.getAddressRange();
        for (int i : addressRange) {
          logger.log(DeviceLogMessage.I2C_BUS_ALLOCATING_ADDRESS, "0x" + Integer.toHexString(i), device.getName());
          mappedDevices.computeIfAbsent(i, k -> new ArrayList<>()).add(device);
        }
      }
    }

  }

  @Override
  public void scanForDevices(long pollDelay) throws InterruptedException {
    if (!active) return;
    List<Integer> foundDevices = new ArrayList<>(mappedDevices.keySet());
    for (Integer addr : foundDevices) {
      List<I2CDeviceController> devices = mappedDevices.get(addr);
      if (devices != null && !devices.isEmpty()) {
        try {
          processDeviceList(addr, devices.get(0));
        } catch (IOException e) {
        }
      }
    }
  }

  private void processDeviceList(int addr, I2CDeviceController deviceEntry) throws IOException {
    I2CDeviceImpl i2CDevice = new I2CMockDeviceImpl(i2cBus, addr);
    I2CDeviceController device = deviceEntry.mount(i2CDevice);
    I2CDeviceController controller = new I2CDeviceScheduler(device);
    activeDevices.put(Integer.toHexString(addr), controller);
  }


}
