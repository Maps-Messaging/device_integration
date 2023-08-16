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
import io.mapsmessaging.devices.impl.I2CDeviceImpl;
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
  private final int i2cBus;
  private final boolean active;

  public I2CBusManager(Context pi4j, I2CProvider i2cProvider, int bus) {
    logger.log(DeviceLogMessage.I2C_BUS_MANAGER_STARTUP);
    boolean enableBus0 = Boolean.parseBoolean(System.getProperty("i2cbus0", "false"));
    active = bus != 0 || enableBus0;
    i2cBus = bus;
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

  public I2CDeviceController configureDevice(int address, String name) throws IOException {
    Map<String, Object> map = new LinkedHashMap<>();
    Map<String, Object> config = new LinkedHashMap<>();
    config.put("deviceName", name);
    map.put("" + address, config);
    return configureDevices(map);
  }

  public I2CDeviceController configureDevices(Map<String, Object> configuration) throws IOException {
    for (Map.Entry<String, Object> entry : configuration.entrySet()) {
      int i2cAddress = Integer.parseInt(entry.getKey());
      Map<String, Object> deviceConfig = (Map<String, Object>) entry.getValue();
      // Retrieve the device name from the configuration
      String deviceName = (String) deviceConfig.get("deviceName");
      // Find the matching device in the known devices list
      I2CDeviceController deviceEntry = knownDevices.get(deviceName);
      if (deviceEntry != null) {
        logger.log(DeviceLogMessage.I2C_BUS_CONFIGURING_DEVICE, deviceEntry.getName(), i2cAddress);
        return createAndMountDevice(i2cAddress, deviceEntry);
      } else {
        logger.log(DeviceLogMessage.I2C_BUS_DEVICE_NOT_FOUND, deviceName);
      }
    }
    return null;
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
    if (!active) return;
    List<Integer> foundDevices = findDevicesOnBus();
    for (Integer addr : foundDevices) {
      List<I2CDeviceController> devices = mappedDevices.get(addr);
      if (devices != null && !devices.isEmpty()) {
        processDeviceList(addr, devices);
      }
    }
  }

  private void processDeviceList(int addr, List<I2CDeviceController> devices) {
    if (devices.size() == 1) {
      try {
        createAndMountDevice(addr, devices.get(0));
      } catch (IOException e) {
        // Log here
      }
    } else {
      scanAddrCollisions(addr, devices);
    }
  }

  private void scanAddrCollisions(int addr, List<I2CDeviceController> devices) {
    boolean located = false;
    for (I2CDeviceController device : devices) {
      I2CDeviceImpl i2CDevice = new I2CDeviceImpl(physicalDevices.get(addr));
      if (device.canDetect() && device.detect(i2CDevice)) {
        try {
          createAndMountDevice(addr, device);
          located = true;
        } catch (IOException e) {
          // Log here
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

  public List<Integer> findDevicesOnBus() {
    List<Integer> found = new ArrayList<>();
    for (int x = 0; x < 0x78; x++) {
      if (!activeDevices.containsKey(Integer.toHexString(x))) {
        try {
          I2C device = physicalDevices.get(x);
          if (device == null) {
            device = createi2cDevice(x);
          }
          if (isOnBus(x, device)) {
            found.add(x);
          }
        } catch (Exception e) {
          // Ignore since we are simply looking for devices
        }
      }
    }
    listDetected(found);
    return found;
  }

  private I2C createi2cDevice(int addr) {
    I2CConfig i2cConfig = I2C.newConfigBuilder(pi4j)
        .id("Device::" + Integer.toHexString(addr))
        .description("Device::" + Integer.toHexString(addr))
        .bus(i2cBus)
        .device(addr)
        .build();
    I2C device = i2cProvider.create(i2cConfig);
    physicalDevices.put(addr, device);
    return device;
  }

  private boolean isOnBus(int addr, I2C device) {
    try {
      byte[] buf = new byte[1];
      if (addr == 0x5c) {
        device.read(buf, 0, 1);
        TimeUnit.MILLISECONDS.sleep(20);
      }
      TimeUnit.MILLISECONDS.sleep(1);
      return device.read(buf, 0, 1) == 1;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (Exception ex) {
      // Ignore
    }
    return false;
  }

  private I2CDeviceController createAndMountDevice(int i2cAddress, I2CDeviceController deviceEntry) throws IOException {
    I2C i2c = physicalDevices.get(i2cAddress);
    if (i2c == null) {
      i2c = createi2cDevice(i2cAddress);
    }
    I2CDeviceImpl i2CDevice = new I2CDeviceImpl(i2c);
    I2CDeviceController device = deviceEntry.mount(i2CDevice);
    I2CDeviceController controller = new I2CDeviceScheduler(device);
    activeDevices.put(Integer.toHexString(i2cAddress), controller);
    return controller;
  }

  public List<String> listDetected(List<Integer> found) {
    List<Integer> activeList = new ArrayList<>();
    for (DeviceController controller : activeDevices.values()) {
      activeList.add(((I2CDeviceController) controller).getMountedAddress());
    }
    int addr = 0;
    List<String> scanResult = new ArrayList<>();
    scanResult.add("I2C Device on bus " + i2cBus);
    scanResult.add("     0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f");
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
      scanResult.add(sb.toString());
    }
    for (String line : scanResult) {
      logger.log(I2C_BUS_SCAN, line);
    }
    return scanResult;
  }


}
