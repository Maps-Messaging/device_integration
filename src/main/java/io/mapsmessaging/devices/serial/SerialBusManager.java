/*
 *
 *  Copyright [ 2020 - 2024 ] Matthew Buckton
 *  Copyright [ 2024 - 2026 ] MapsMessaging B.V.
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

package io.mapsmessaging.devices.serial;

import io.mapsmessaging.devices.DeviceController;
import io.mapsmessaging.devices.ComputedDeviceRegistration;
import java.util.ArrayList;
import io.mapsmessaging.devices.serial.devices.sensors.SerialDevice;

import java.io.IOException;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

public class SerialBusManager {

  private final Map<String, SerialDeviceController> knownDevices;
  private final Map<String, DeviceController> activeDevices;

  public SerialBusManager() {
    knownDevices = new ConcurrentHashMap<>();
    activeDevices = new ConcurrentHashMap<>();
    ServiceLoader<SerialDeviceController> known = ServiceLoader.load(SerialDeviceController.class);
    for (SerialDeviceController device : known) {
      knownDevices.put(device.getName(), device);
    }
  }

  public SerialDeviceController getDevice(String name) {
    return knownDevices.get(name);
  }


  public Map<String, DeviceController> getActive() {
    return activeDevices;
  }

  public synchronized SerialDeviceController mount(String name, SerialDevice serialDevice) throws IOException {
    SerialDeviceController controller = knownDevices.get(name);
    if (controller != null) {
      DeviceController current = activeDevices.get(name);
      if (current instanceof SerialDeviceController mounted) {
        return mounted;
      }
      controller = controller.mount(serialDevice);
      Map<String, SerialDeviceController> computed;
      try {
        computed = ComputedDeviceRegistration.collect(controller, name, SerialDeviceController.class, activeDevices);
        for (SerialDeviceController secondary : computed.values()) {
          if (!(secondary instanceof ComputedSerialDeviceController)) {
            throw new IOException("Secondary serial controllers must extend ComputedSerialDeviceController");
          }
        }
      } catch (IOException | RuntimeException exception) {
        controller.close();
        throw exception;
      }
      activeDevices.put(name, controller);
      activeDevices.putAll(computed);
    }
    return controller;
  }

  public synchronized void unmount(SerialDeviceController serialDevice) {
    String key = serialDevice.getName();
    DeviceController current = activeDevices.get(key);
    if (current != serialDevice) return;
    if (serialDevice.getComputedDeviceSuffix().isEmpty()) {
      // Primary ownership remains with the caller, as in the existing serial API.
      for (String childKey : new ArrayList<>(activeDevices.keySet())) {
        if (childKey.startsWith(key + "_")) {
          DeviceController secondary = activeDevices.remove(childKey);
          if (secondary != null) secondary.close();
        }
      }
    } else {
      // Only the computed controller's logical close is invoked.
      serialDevice.close();
    }
    activeDevices.remove(key, serialDevice);
  }

}
