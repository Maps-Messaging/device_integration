/*
 *    Copyright [ 2020 - 2024 ] Matthew Buckton
 *    Copyright [ 2024 - 2025 ] MapsMessaging B.V.
 *
 *    Licensed under the Apache License, Version 2.0 with the Commons Clause
 *    (the "License"); you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at:
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *        https://commonsclause.com/
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License
 */

package io.mapsmessaging.devices.serial;

import io.mapsmessaging.devices.DeviceController;
import io.mapsmessaging.devices.serial.devices.sensors.SerialDevice;

import java.io.IOException;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

public class SerialBusManager {

  private Map<String, SerialDeviceController> knownDevices;
  private Map<String, DeviceController> activeDevices;

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

  public SerialDeviceController mount(String name, SerialDevice serialDevice) throws IOException {
    SerialDeviceController controller = knownDevices.get(name);
    if (controller != null) {
      controller = controller.mount(serialDevice);
      activeDevices.put(name, controller);
    }
    return controller;
  }

  public void unmount (SerialDeviceController serialDevice) throws IOException {
    activeDevices.remove(serialDevice.getName());
  }

}
