package io.mapsmessaging.server.i2c;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import com.pi4j.io.i2c.I2CProvider;
import io.mapsmessaging.server.i2c.devices.output.led.Quad7SegmentManager;
import io.mapsmessaging.server.i2c.devices.sensors.TLS2561Manager;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;
import org.json.JSONObject;

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

  public void scanForDevices() throws Exception {
    Context pi4j = Pi4J.newAutoContext();
    I2CProvider i2CProvider = pi4j.provider("linuxfs-i2c");
/*
    int[] devices = {0x39, 0x72};
    I2CConfig i2cConfig = I2C.newConfigBuilder(pi4j)
        .id("Device::" + Integer.toHexString(0x39))
        .description("Device::" + Integer.toHexString(0x39))
        .bus(1)
        .device(0x39)
        .build();
    I2C device = i2CProvider.create(i2cConfig);
    TLS2561Manager light = new TLS2561Manager(device);
    I2CConfig i2cConfig2 = I2C.newConfigBuilder(pi4j)
        .id("Device::" + Integer.toHexString(0x72))
        .description("Device::" + Integer.toHexString(0x72))
        .bus(1)
        .device(0x72)
        .build();
    I2C device2 = i2CProvider.create(i2cConfig2);
    Quad7SegmentManager display = new Quad7SegmentManager(device2);

    for(int x=0;x<1000;x++){
      TimeUnit.MILLISECONDS.sleep(50);
      JSONObject object = new JSONObject(new String(light.getPayload()));
      String val = String.valueOf(object.getInt("Light"));
      while(val.length() < 4){
        val = " "+val;
      }
      val = val.substring(0, 2)+' '+val.substring(2);
      display.setPayload(val.getBytes());
    }
*/
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

  public static void main(String[] args) throws Exception {
    I2CBusManager i2CBusManager = new I2CBusManager();
    i2CBusManager.scanForDevices();

  }
}
