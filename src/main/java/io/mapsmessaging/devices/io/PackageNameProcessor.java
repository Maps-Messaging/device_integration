package io.mapsmessaging.devices.io;

import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("java:S6548") // yes it is a singleton
public class PackageNameProcessor {

  protected static final String[][] MAPPING = {
      {"io.mapsmessaging.devices.i2c.devices.sensors.", "#i2c_snr#"},
      {"io.mapsmessaging.devices.i2c.devices.rtc.", "#i2c_rtc#"},
      {"io.mapsmessaging.devices.i2c.devices.output.", "#i2c_out#"},
      {"io.mapsmessaging.devices.i2c.devices.drivers.", "#i2c_drv#"},
      {"io.mapsmessaging.devices.i2c.devices.storage.", "#i2c_str#"},
      {"io.mapsmessaging.devices.spi.devices.", "#spi#"},
      {"io.mapsmessaging.devices.onewire.devices.", "#1wire#"},
  };

  private static class Holder {
    private static final PackageNameProcessor INSTANCE = new PackageNameProcessor();
  }

  // Global access point to get the Singleton instance
  public static PackageNameProcessor getInstance() {
    return Holder.INSTANCE;
  }


  private final Map<String, String> byPackageName;

  private PackageNameProcessor() {
    byPackageName = new LinkedHashMap<>();
    for (String[] map : MAPPING) {
      byPackageName.put(map[0], map[1]);
    }
  }

  public String getPrefix(String packageName) {
    for (Map.Entry<String, String> entry : byPackageName.entrySet()) {
      if (packageName.startsWith(entry.getKey())) {
        String name = entry.getValue() + packageName.substring(entry.getKey().length());
        int idx = name.indexOf(".data.");
        if (idx > 0) {
          name = name.substring(0, idx) + "@" + name.substring(idx + ".data.".length());
        }
        return name;
      }
    }
    return packageName;
  }

  public String getPackage(String id) {
    for (Map.Entry<String, String> entry : byPackageName.entrySet()) {
      if (id.startsWith(entry.getValue())) {
        id = entry.getKey() + id.substring(entry.getValue().length());
        int idx = id.indexOf("@");
        if( idx >=0) {
          id = id.substring(0, idx) + ".data." + id.substring(idx + 1);
        }
      }
    }
    return id;
  }
}


