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

package io.mapsmessaging.devices.onewire;

import io.mapsmessaging.devices.DeviceController;
import io.mapsmessaging.devices.logging.DeviceLogMessage;
import io.mapsmessaging.logging.Logger;
import io.mapsmessaging.logging.LoggerFactory;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

public class OneWireBusManager {
  private static final String ONE_WIRE_ROOT_PATH = "/sys/bus/w1/devices/";

  private final Logger logger = LoggerFactory.getLogger(OneWireBusManager.class);

  private final Map<String, OneWireDeviceEntry> knownDevices;
  private final Map<String, DeviceController> activeDevices;

  private final File rootDirectory;

  public OneWireBusManager() {
    logger.log(DeviceLogMessage.ONE_WIRE_BUS_MANAGER_STARTUP, ONE_WIRE_ROOT_PATH);

    knownDevices = new LinkedHashMap<>();
    activeDevices = new ConcurrentHashMap<>();
    rootDirectory = new File(ONE_WIRE_ROOT_PATH);
    if (rootDirectory.exists()) {
      ServiceLoader<OneWireDeviceEntry> deviceEntries = ServiceLoader.load(OneWireDeviceEntry.class);
      for (OneWireDeviceEntry device : deviceEntries) {
        knownDevices.put(device.getId(), device);
      }
      scan();
    }
  }

  public OneWireDeviceEntry get(String id) {
    return (OneWireDeviceEntry) activeDevices.get(id);
  }

  public Map<String, DeviceController> getActive() {
    return activeDevices;
  }

  public void scan() {
    File[] files = rootDirectory.listFiles();
    if (files == null) return;
    for (File device : files) {
      for (Map.Entry<String, OneWireDeviceEntry> entry : knownDevices.entrySet()) {
        if (device.getName().startsWith(entry.getKey())) {
          File data = new File(device, "w1_slave");
          if (data.exists()) {
            String path = device.getName();
            activeDevices.computeIfAbsent(path, s -> entry.getValue().mount(data));
          }
        }
      }
    }
  }
}
