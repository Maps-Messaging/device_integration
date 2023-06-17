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

package io.mapsmessaging.devices.oneWire;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class OneWireBusManager {

  private final Map<String, OneWireDeviceEntry> knownDevices;
  private final Map<String, OneWireDeviceEntry> activeDevices;

  private final File rootDirectory;

  public OneWireBusManager(){
    knownDevices = new LinkedHashMap<>();
    activeDevices = new ConcurrentHashMap<>();
    rootDirectory = new File("/sys/bus/w1/devices/");
    if(rootDirectory.exists()) {
      ServiceLoader<OneWireDeviceEntry> deviceEntries = ServiceLoader.load(OneWireDeviceEntry.class);
      for (OneWireDeviceEntry device : deviceEntries) {
        knownDevices.put(device.getId(), device);
      }
      scan();
    }
  }

  public OneWireDeviceEntry get(String id){
    return activeDevices.get(id);
  }

  public Map<String, OneWireDeviceEntry> getActive(){
    return activeDevices;
  }

  public void scan() {
    File[] files = rootDirectory.listFiles();
    for (File device : files) {
      for(String id:knownDevices.keySet()) {
        if(device.getName().startsWith(id)) {
          File data = new File(device, "w1_slave");
          if (data.exists()) {
            String path = data.toString();
            if (!activeDevices.containsKey(path)) {
              OneWireDeviceEntry entry = knownDevices.get(id);
              activeDevices.put(path, entry.mount(data));
            }
          }
        }
      }
    }
  }
}
