package io.mapsmessaging.server.i2c;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import com.pi4j.io.i2c.I2CProvider;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;

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

  public void scanForDevices(){
    Context pi4j = Pi4J.newAutoContext();
    I2CProvider i2CProvider = pi4j.provider("linuxfs-i2c");
    for(int x=0;x<0x77;x++) {
      I2CDeviceEntry deviceEntry = knownDevices.get(x);
      if(deviceEntry != null) {
        try {
          I2CConfig i2cConfig = I2C.newConfigBuilder(pi4j)
              .id("Device::" + Integer.toHexString(x))
              .description("Device::" + Integer.toHexString(x))
              .bus(1)
              .device(x)
              .build();
          I2C device = i2CProvider.create(i2cConfig);
          try {
            device.read();
            I2CDeviceEntry physicalDevice = deviceEntry.mount(device);
            for(int y=0;y<1000;y++){
              System.err.println(new String(physicalDevice.getPayload()));
              TimeUnit.MILLISECONDS.sleep(500);
            }
            System.err.println("Scan found "+deviceEntry.getSchema().getComments());
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          device.close();
        } catch (Exception e) {
        }
      }
    }

  }

  public static void main(String[] args) {
    I2CBusManager i2CBusManager = new I2CBusManager();
    i2CBusManager.scanForDevices();

  }
}
