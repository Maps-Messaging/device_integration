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
import io.mapsmessaging.devices.serial.devices.sensors.SerialDevice;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import java.io.IOException;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

public class SerialBusManager {

  private final Map<String, SerialDeviceController> knownDevices;
  private final Map<String, DeviceController> activeDevices;
  private final Map<String, Map<String, SerialDeviceController>> computedDevices;

  public SerialBusManager() {
    knownDevices = new ConcurrentHashMap<>();
    activeDevices = new ConcurrentHashMap<>();
    computedDevices = new ConcurrentHashMap<>();
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

  /**
   * Register the physical controller under its serial port, and logical controllers
   * under port_suffix, as I2C does with address and address_suffix.
   */
  public synchronized SerialDeviceController mount(String name, SerialDevice serialDevice) throws IOException {
    SerialDeviceController factory = knownDevices.get(name);
    if (factory == null) return null;

    if (serialDevice == null || serialDevice.getSystemPortName() == null
        || serialDevice.getSystemPortName().isBlank()) {
      throw new IOException("Serial device must have a physical port name");
    }
    String portName = serialDevice.getSystemPortName();
    DeviceController existing = activeDevices.get(portName);
    if (existing != null) {
      if (existing instanceof SerialDeviceController mounted && mounted.getName().equals(name)) {
        return mounted;
      }
      throw new IOException("Serial port is already mounted: " + portName);
    }

    SerialDeviceController controller = factory.mount(serialDevice);
    Map<String, SerialDeviceController> computed;
    try {
      computed = ComputedDeviceRegistration.collect(controller, portName, SerialDeviceController.class, activeDevices);
      for (SerialDeviceController secondary : computed.values()) {
        if (!(secondary instanceof ComputedSerialDeviceController)) {
          throw new IOException("Secondary serial controllers must extend ComputedSerialDeviceController");
        }
      }
    } catch (IOException | RuntimeException exception) {
      controller.close();
      throw exception;
    }

    activeDevices.put(portName, controller);
    activeDevices.putAll(computed);
    computedDevices.put(portName, new LinkedHashMap<>(computed));
    return controller;
  }

  /** Detach an individual logical device, or every logical child of its physical owner. */
  public synchronized void unmount(SerialDeviceController serialDevice) {
    String key = null;
    for (Entry<String, DeviceController> entry : activeDevices.entrySet()) {
      if (entry.getValue() == serialDevice) {
        key = entry.getKey();
        break;
      }
    }
    if (key == null) return;

    if (computedDevices.containsKey(key)) {
      Map<String, SerialDeviceController> children = computedDevices.remove(key);
      for (Entry<String, SerialDeviceController> child : children.entrySet()) {
        if (activeDevices.remove(child.getKey(), child.getValue())) {
          child.getValue().close();
        }
      }
      // Preserve the existing API: unmount unregisters but does not close the primary.
      activeDevices.remove(key, serialDevice);
    } else {
      if (activeDevices.remove(key, serialDevice)) {
        for (Map<String, SerialDeviceController> children : computedDevices.values()) {
          children.remove(key);
        }
        serialDevice.close();
      }
    }
  }
}
