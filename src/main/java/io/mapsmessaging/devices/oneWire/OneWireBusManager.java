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
