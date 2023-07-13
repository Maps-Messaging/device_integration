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
import io.mapsmessaging.devices.logging.DeviceLogMessage;
import io.mapsmessaging.logging.Logger;
import io.mapsmessaging.logging.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static io.mapsmessaging.devices.logging.DeviceLogMessage.I2C_BUS_SCAN;
import static io.mapsmessaging.devices.logging.DeviceLogMessage.I2C_BUS_SCAN_MULTIPLE_DEVICES;

public class I2CBusManager {
  private final Logger logger = LoggerFactory.getLogger(I2CBusManager.class);

  private final Map<String, I2CDeviceController> knownDevices;
  private final Map<Integer, List<I2CDeviceController>> mappedDevices;
  private final Map<String, DeviceController> activeDevices;
  private final Map<Integer, I2C> physicalDevices;

  private final Context pi4j;
  private final I2CProvider i2cProvider;


  public I2CBusManager(Context pi4j, I2CProvider i2cProvider) {
    logger.log(DeviceLogMessage.I2C_BUS_MANAGER_STARTUP);
    this.pi4j = pi4j;
    this.i2cProvider = i2cProvider;
    mappedDevices = new LinkedHashMap<>();
    activeDevices = new ConcurrentHashMap<>();
    knownDevices = new ConcurrentHashMap<>();
    physicalDevices = new ConcurrentHashMap<>();
    ServiceLoader<I2CDeviceController> deviceEntries = ServiceLoader.load(I2CDeviceController.class);
    for (I2CDeviceController device : deviceEntries) {
      knownDevices.put(device.getName(), device);
      logger.log(DeviceLogMessage.I2C_BUS_LOADED_DEVICE, device.getName());
      int[] addressRange = device.getAddressRange();
      for (int i : addressRange) {
        logger.log(DeviceLogMessage.I2C_BUS_ALLOCATING_ADDRESS, "0x" + Integer.toHexString(i), device.getName());
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
      I2CDeviceController deviceEntry = knownDevices.get(deviceName);
      if (deviceEntry != null) {
        logger.log(DeviceLogMessage.I2C_BUS_CONFIGURING_DEVICE, deviceEntry.getName(), i2cAddress);
        createAndMountDevice(i2cAddress, deviceEntry);
      } else {
        logger.log(DeviceLogMessage.I2C_BUS_DEVICE_NOT_FOUND, deviceName);
      }
    }
  }

  public void close(I2CDeviceController deviceController) {
    deviceController.close();
    String key = Integer.toHexString(deviceController.getMountedAddress());
    activeDevices.remove(key);
  }

  public I2CDeviceController get(String id) {
    return (I2CDeviceController) activeDevices.get(id);
  }

  public Map<String, DeviceController> getActive() {
    return activeDevices;
  }

  public void scanForDevices() {
    List<Integer> foundDevices = findDevicesOnBus();
    for (Integer addr : foundDevices) {
      List<I2CDeviceController> devices = mappedDevices.get(addr);
      if (devices != null && !devices.isEmpty()) {
        if (devices.size() == 1) {
          try {
            createAndMountDevice(addr, devices.get(0));
          } catch (IOException e) {
            e.printStackTrace();
          }
        } else {
          boolean located = false;
          for (I2CDeviceController device : devices) {
            if (device.canDetect() && device.detect(physicalDevices.get(addr))) {
              try {
                createAndMountDevice(addr, device);
                located = true;
              } catch (IOException e) {
                e.printStackTrace();
              }
            }
          }
          if (!located) {
            StringBuilder sb = new StringBuilder();
            for (I2CDeviceController controller : devices) {
              sb.append(controller.getName()).append(" ");
            }
            logger.log(I2C_BUS_SCAN_MULTIPLE_DEVICES, sb.toString());
          }
        }
      }
    }
  }

  public List<Integer> findDevicesOnBus() {
    List<Integer> found = new ArrayList<>();
    for (int x = 0; x < 0x77; x++) {
      if (!activeDevices.containsKey(Integer.toHexString(x))) {
        try {
          I2C device = physicalDevices.get(x);
          if (device == null) {
            I2CConfig i2cConfig = I2C.newConfigBuilder(pi4j)
                .id("Device::" + Integer.toHexString(x))
                .description("Device::" + Integer.toHexString(x))
                .bus(1)
                .device(x)
                .build();
            device = i2cProvider.create(i2cConfig);
            physicalDevices.put(x, device);
          }
          if (isOnBus(x, device)) {
            found.add(x);
          }
        } catch (Exception e) {
          // Ignore since we are simply looking for devices
        }
      }
    }
    logDetect(found);
    return found;
  }

  private boolean isOnBus(int addr, I2C device) {
    try {
      byte[] buf = new byte[1];
      if (addr == 0x5c) {
        device.read(buf, 0, 1);
        TimeUnit.MILLISECONDS.sleep(20);
      }
      return device.read(buf, 0, 1) == 1;
    } catch (Exception ex) {
      return false;
    }
  }

  private void createAndMountDevice(int i2cAddress, I2CDeviceController deviceEntry) throws IOException {
    I2CDeviceController device = deviceEntry.mount(physicalDevices.get(i2cAddress));
    activeDevices.put(Integer.toHexString(i2cAddress), new I2CDeviceScheduler(device));
  }

  private void logDetect(List<Integer> found) {
    List<Integer> activeList = new ArrayList<>();
    for (DeviceController controller : activeDevices.values()) {
      activeList.add(((I2CDeviceController) controller).getMountedAddress());
    }
    int addr = 0;
    logger.log(I2C_BUS_SCAN, "     0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f");
    for (int x = 0; x < 8; x++) {
      StringBuilder sb = new StringBuilder(x + "0: ");
      for (int y = 0; y < 16; y++) {
        String display;
        if (activeList.contains(addr)) {
          display = "AA";
        } else if (found.contains(addr)) {
          display = Integer.toHexString(addr);
          if (addr < 16) {
            display = "0" + display;
          }
        } else {
          display = "--";
        }
        sb.append(display).append(" ");
        addr++;
      }
      logger.log(I2C_BUS_SCAN, sb.toString());
    }
  }


}
