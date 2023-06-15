package io.mapsmessaging.server.devices.i2c;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import com.pi4j.io.i2c.I2CProvider;
import lombok.Getter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

public class I2CBusManager {


  private final Map<Integer, I2CDeviceEntry> knownDevices;
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
        knownDevices.put(i, device);
      }
    }
  }

  public void scanForDevices() {
    System.err.println("Scanning for devices");
    for(int x=0;x<0x77;x++) {
      if(!activeDevices.containsKey(Integer.toHexString(x))) {
        System.err.println("Scanning address "+Integer.toHexString(x));
        I2CDeviceEntry deviceEntry = knownDevices.get(x);
        if (deviceEntry != null) {
          System.err.println("Have a known devive as this address "+deviceEntry.getClass().getName());
          I2CConfig i2cConfig = I2C.newConfigBuilder(pi4j)
                  .id("Device::" + Integer.toHexString(x))
                  .description("Device::" + Integer.toHexString(x))
                  .bus(1)
                  .device(x)
                  .build();
          I2C device = i2CProvider.create(i2cConfig);
          try {
            byte[] buf = new byte[1];
            device.read(buf, 0, 1);
            I2CDeviceEntry physicalDevice = deviceEntry.mount(device);
            System.err.println("Added new device "+Integer.toHexString(x)+" "+physicalDevice.getClass().getName());
            activeDevices.put(Integer.toHexString(x), physicalDevice);
          } catch (Throwable e) {
            device.close();
            // No such device
          }
        }
      }
    }
    System.err.println("Scanned for devices");
  }

  public I2CDeviceEntry get(String id) {
    return activeDevices.get(id);
  }
}
