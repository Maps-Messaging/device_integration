package io.mapsmessaging.devices.spi;

import com.pi4j.context.Context;
import io.mapsmessaging.devices.DeviceController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

public class SpiBusManager {

  private final Map<String, SpiDeviceController> knownDevices;
  private final Map<String, DeviceController> activeDevices;

  private final Context pi4j;

  public SpiBusManager(Context pi4j) {
    this.pi4j = pi4j;
    knownDevices = new LinkedHashMap<>();
    activeDevices = new ConcurrentHashMap<>();
    ServiceLoader<SpiDeviceController> deviceEntries = ServiceLoader.load(SpiDeviceController.class);
    for (SpiDeviceController controller : deviceEntries) {
      knownDevices.putIfAbsent(controller.getName(), controller);
    }
  }

  public void configureDevices(Map<String, Object> configuration){
    for (Map.Entry<String, Object> entry : configuration.entrySet()) {
      String spiName = entry.getKey();
      Map<String, String> deviceConfig = (Map<String, String>) entry.getValue();
      mount(spiName, deviceConfig);
    }
  }

  public SpiDeviceController mount(String name, Map<String, String> config) {
    SpiDeviceController controller = knownDevices.get(name);
    if (controller != null) {
      SpiDeviceController mounted = controller.mount(pi4j, config);
      activeDevices.put(mounted.getName(), mounted);
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
