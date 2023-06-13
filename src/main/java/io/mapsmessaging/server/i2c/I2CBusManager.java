package io.mapsmessaging.server.i2c;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceLoader;

public class I2CBusManager {


  private final Map<Integer, I2CDeviceEntry> knownDevices;

  public I2CBusManager() {
    knownDevices = new LinkedHashMap<>();
    ServiceLoader<I2CDeviceEntry> deviceEntries = ServiceLoader.load(I2CDeviceEntry.class);
    for (I2CDeviceEntry device : deviceEntries) {
      int[] addressRange = device.getAddressRange();
      for (int i : addressRange) {
        knownDevices.put(i, device);
      }
    }
  }

  public static void main(String[] args) throws IOException {
    new I2CBusManager();
  }
}
