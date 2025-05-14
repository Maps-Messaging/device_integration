/*
 *
 *  Copyright [ 2020 - 2024 ] [Matthew Buckton]
 *  Copyright [ 2024 - 2025.  ] [Maps Messaging B.V.]
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

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
  private final Map<String, String> byPackageName;

  private PackageNameProcessor() {
    byPackageName = new LinkedHashMap<>();
    for (String[] map : MAPPING) {
      byPackageName.put(map[0], map[1]);
    }
  }

  // Global access point to get the Singleton instance
  public static PackageNameProcessor getInstance() {
    return Holder.INSTANCE;
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
        if (idx >= 0) {
          id = id.substring(0, idx) + ".data." + id.substring(idx + 1);
        }
      }
    }
    return id;
  }

  private static class Holder {
    private static final PackageNameProcessor INSTANCE = new PackageNameProcessor();
  }
}


