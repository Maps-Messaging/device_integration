package io.mapsmessaging.devices.i2c;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import com.pi4j.io.i2c.I2CProvider;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class I2CBusManager {

  private final Map<Integer, List<I2CDeviceEntry>> knownDevices;
  private final Map<String, I2CDeviceEntry> activeDevices;

  private final Context pi4j = Pi4J.newAutoContext();
  private final I2CProvider i2CProvider = pi4j.provider("linuxfs-i2c");


  public I2CBusManager() {
    knownDevices = new LinkedHashMap<>();
    activeDevices = new ConcurrentHashMap<>();
    ServiceLoader<I2CDeviceEntry> deviceEntries = ServiceLoader.load(I2CDeviceEntry.class);
    for (I2CDeviceEntry device : deviceEntries) {
      int[] addressRange = device.getAddressRange();
      for (int i : addressRange) {
        knownDevices.computeIfAbsent(i, k -> new ArrayList<>()).add(device);
      }
    }
  }

  public Map<String, I2CDeviceEntry> getActive(){
    return activeDevices;
  }

  public void scanForDevices() {
    for(int x=0;x<0x77;x++) {
      if(!activeDevices.containsKey(Integer.toHexString(x))) {
        List<I2CDeviceEntry> deviceList = knownDevices.get(x);
        if (deviceList != null && !deviceList.isEmpty()) {
          I2CConfig i2cConfig = I2C.newConfigBuilder(pi4j)
              .id("Device::" + Integer.toHexString(x))
              .description("Device::" + Integer.toHexString(x))
              .bus(1)
              .device(x)
              .build();
          I2C device = i2CProvider.create(i2cConfig);
          for (I2CDeviceEntry deviceEntry : deviceList) {
            attemptToConnect(x, device, deviceEntry);
          }
        }
      }
    }
  }

  private void attemptToConnect(int addr,I2C device, I2CDeviceEntry deviceEntry )  {
    System.err.println("Have a known device as this address " + deviceEntry.getClass().getName());
    if(isOnBus(addr, device)) {
      try {
        I2CDeviceEntry physicalDevice = deviceEntry.mount(device);
        if(physicalDevice.detect()) {
          System.err.println("Added new device " + Integer.toHexString(addr) + " " + physicalDevice.getClass().getName());
          activeDevices.put(Integer.toHexString(addr), physicalDevice);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    else{
      device.close();
    }
  }

  private boolean isOnBus(int addr,I2C device){
    try {
      byte[] buf = new byte[1];
      if (addr == 0x5c) {
        try {
          device.read(buf, 0, 1);
        } catch (Exception e) {
          // Ignore first read
          TimeUnit.MILLISECONDS.sleep(20);
        }
      }
      device.read(buf, 0, 1);
      return true;
    }
    catch(Exception ex){
      return false;
    }
  }

  public I2CDeviceEntry get(String id) {
    return activeDevices.get(id);
  }
}
