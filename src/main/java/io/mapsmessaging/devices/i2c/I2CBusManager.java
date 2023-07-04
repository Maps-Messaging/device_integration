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

package io.mapsmessaging.devices.i2c;

import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import com.pi4j.io.i2c.I2CProvider;
import io.mapsmessaging.devices.DeviceController;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class I2CBusManager {

  private final Map<String, I2CDeviceEntry> knownDevices;
  private final Map<Integer, List<I2CDeviceEntry>> mappedDevices;
  private final Map<String, DeviceController> activeDevices;

  private final Context pi4j;
  private final I2CProvider i2cProvider;


  public I2CBusManager(Context pi4j, I2CProvider i2cProvider) {
    this.pi4j = pi4j;
    this.i2cProvider = i2cProvider;
    mappedDevices = new LinkedHashMap<>();
    activeDevices = new ConcurrentHashMap<>();
    knownDevices = new ConcurrentHashMap<>();
    ServiceLoader<I2CDeviceEntry> deviceEntries = ServiceLoader.load(I2CDeviceEntry.class);
    for (I2CDeviceEntry device : deviceEntries) {
      knownDevices.put(device.getName(), device);
      int[] addressRange = device.getAddressRange();
      for (int i : addressRange) {
        mappedDevices.computeIfAbsent(i, k -> new ArrayList<>()).add(device);
      }
    }
  }

  public void configureDevices(Map<String, Object> configuration) throws IOException {
    for (Map.Entry<String, Object> entry : configuration.entrySet()) {
      int i2cAddress = Integer.parseInt(entry.getKey());
      Map<String, Object> deviceConfig = (Map<String, Object>) entry.getValue();

      // Retrieve the device name from the configuration
      String deviceName = (String) deviceConfig.get("deviceName");

      // Find the matching device in the known devices list
      I2CDeviceEntry deviceEntry = knownDevices.get(deviceName);
      if (deviceEntry != null) {
        createAndMountDevice(i2cAddress, deviceEntry);
      } else {
        System.out.println("Device entry not found for device name: " + deviceName);
      }
    }
  }

  public I2CDeviceEntry get(String id) {
    return (I2CDeviceEntry) activeDevices.get(id);
  }

  public Map<String, DeviceController> getActive() {
    return activeDevices;
  }

  public void scanForDevices() {
    for (int x = 0; x < 0x77; x++) {
      if (!activeDevices.containsKey(Integer.toHexString(x))) {
        List<I2CDeviceEntry> deviceList = mappedDevices.get(x);
        if (deviceList != null && !deviceList.isEmpty()) {
          try {
            I2CConfig i2cConfig = I2C.newConfigBuilder(pi4j)
                .id("Device::" + Integer.toHexString(x))
                .description("Device::" + Integer.toHexString(x))
                .bus(1)
                .device(x)
                .build();
            I2C device = i2cProvider.create(i2cConfig);
            for (I2CDeviceEntry deviceEntry : deviceList) {
              attemptToConnect(x, device, deviceEntry);
            }
          } catch (Exception e) {
            // Ignore
          }
        }
      }
    }
  }

  private void attemptToConnect(int addr, I2C device, I2CDeviceEntry deviceEntry) {
    if (isOnBus(addr, device)) {
      try {
        I2CDeviceEntry physicalDevice = deviceEntry.mount(device);
        if (physicalDevice.detect()) {
          activeDevices.put(Integer.toHexString(addr), physicalDevice);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      device.close();
    }
  }

  private boolean isOnBus(int addr, I2C device) {
    try {
      byte[] buf = new byte[1];
      if (addr == 0x5c) {
        try {
          device.read(buf, 0, 1);
        } catch (Exception e) {
          // Ignore first read
          TimeUnit.MILLISECONDS.sleep(20);
        }
      }
      device.read(buf, 0, 1);
      return true;
    } catch (Exception ex) {
      return false;
    }
  }

  private void createAndMountDevice(int i2cAddress, I2CDeviceEntry deviceEntry) throws IOException {
    I2CConfig i2cConfig = I2C.newConfigBuilder(pi4j)
        .id("Device::" + Integer.toHexString(i2cAddress))
        .description("Device::" + Integer.toHexString(i2cAddress))
        .bus(1)
        .device(i2cAddress)
        .build();
    // Mount the device and get the bound instance
    I2CDeviceEntry device = deviceEntry.mount(i2cProvider.create(i2cConfig));
    activeDevices.put(Integer.toHexString(i2cAddress), device);
  }

}
