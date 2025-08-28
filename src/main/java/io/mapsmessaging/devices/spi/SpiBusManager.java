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

package io.mapsmessaging.devices.spi;

import com.pi4j.context.Context;
import io.mapsmessaging.devices.DeviceController;
import io.mapsmessaging.devices.logging.DeviceLogMessage;
import io.mapsmessaging.logging.Logger;
import io.mapsmessaging.logging.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SpiBusManager {

  private final Logger logger = LoggerFactory.getLogger(SpiBusManager.class);

  private final Map<String, SpiDeviceController> knownDevices;
  private final Map<String, DeviceController> activeDevices;

  private final Context pi4j;

  public SpiBusManager(Context pi4j) {
    logger.log(DeviceLogMessage.SPI_BUS_MANAGER_STARTUP);

    this.pi4j = pi4j;
    knownDevices = new LinkedHashMap<>();
    activeDevices = new ConcurrentHashMap<>();
    ServiceLoader<SpiDeviceController> deviceEntries = ServiceLoader.load(SpiDeviceController.class);
    for (SpiDeviceController controller : deviceEntries) {
      knownDevices.putIfAbsent(controller.getName(), controller);
    }
  }

  public SpiDeviceController configureDevice(String spiName, Map<String, String> configuration) {
    return mount(spiName, configuration);
  }

  public List<SpiDeviceController> configureDevices(Map<String, Object> configuration) {
    List<SpiDeviceController> devices = new ArrayList<>();
    for (Map.Entry<String, Object> entry : configuration.entrySet()) {
      String spiName = entry.getKey();
      Map<String, String> deviceConfig = (Map<String, String>) entry.getValue();
      devices.add(configureDevice(spiName, deviceConfig));
    }
    return devices;
  }

  public SpiDeviceController mount(String name, Map<String, String> config) {
    SpiDeviceController controller = knownDevices.get(name);
    if (controller != null) {
      SpiDeviceController mounted = controller.mount(pi4j, config);
      activeDevices.put(mounted.getName(), new SpiDeviceScheduler(mounted));
      return mounted;
    }
    return null;
  }

  public Map<String, DeviceController> getActive() {
    return activeDevices;
  }

  public SpiDeviceController get(String id) {
    return (SpiDeviceController) activeDevices.get(id);
  }
}
