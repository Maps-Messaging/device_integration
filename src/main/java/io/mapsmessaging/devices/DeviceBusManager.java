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

package io.mapsmessaging.devices;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CProvider;
import io.mapsmessaging.devices.gpio.InterruptFactory;
import io.mapsmessaging.devices.gpio.Pi4JPinManagement;
import io.mapsmessaging.devices.gpio.PiInterruptFactory;
import io.mapsmessaging.devices.i2c.I2CBusManager;
import io.mapsmessaging.devices.logging.DeviceLogMessage;
import io.mapsmessaging.devices.onewire.OneWireBusManager;
import io.mapsmessaging.devices.spi.SpiBusManager;
import io.mapsmessaging.logging.Logger;
import io.mapsmessaging.logging.LoggerFactory;
import lombok.Getter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;


@SuppressWarnings("java:S6548") // yes it is a singleton
@Getter
public class DeviceBusManager {
  // Inner static class responsible for holding the Singleton instance
  private static class Holder {
    private static final DeviceBusManager INSTANCE = new DeviceBusManager();
  }

  // Global access point to get the Singleton instance
  public static DeviceBusManager getInstance() {
    return Holder.INSTANCE;
  }
  private static final String[] PROVIDERS = {"pigpio-i2c", "linuxfs-i2c"};

  private final Logger logger = LoggerFactory.getLogger(DeviceBusManager.class);
  private final Context pi4j;
  private final I2CBusManager[] i2cBusManager;
  private final OneWireBusManager oneWireBusManager;
  private final SpiBusManager spiBusManager;
  private final Pi4JPinManagement pinManagement;
  private final InterruptFactory interruptFactory;
  private final boolean supportsLengthResponse;

  private DeviceBusManager() {
    logger.log(DeviceLogMessage.BUS_MANAGER_STARTUP);
    pi4j = Pi4J.newAutoContext();
    String provider = getProvider();
    supportsLengthResponse = provider.equalsIgnoreCase("linuxfs-i2c");
    I2CProvider i2cProvider = pi4j.provider(provider);
    logger.log(DeviceLogMessage.BUS_MANAGER_PROVIDER, provider);
    i2cBusManager = new I2CBusManager[2];
    for (int x = 0; x < i2cBusManager.length; x++) {
      i2cBusManager[x] = new I2CBusManager(pi4j, i2cProvider, x);
    }
    oneWireBusManager = new OneWireBusManager();
    spiBusManager = new SpiBusManager(pi4j);
    pinManagement = new Pi4JPinManagement(pi4j);
    interruptFactory = new PiInterruptFactory(pi4j);
  }

  public boolean isAvailable(){
    boolean result = false;
    try {
      try (var i2c = pi4j.create(I2C.newConfigBuilder(pi4j).id("Test I2C").device(1).bus(1).build())) {
        i2c.getDevice();
        result = true;
      }
    } catch (Throwable e) {
    }
    return result;
  }
  private static String getProvider() {
    String provider = System.getProperty("I2C-PROVIDER", PROVIDERS[0]).toLowerCase();
    boolean isValid = false;
    for (String providers : PROVIDERS) {
      if (providers.equals(provider)) {
        isValid = true;
        break;
      }
    }
    if (!isValid) {
      provider = PROVIDERS[0];
    }
    return provider;
  }

  public void configureDevices(Map<String, Object> config) throws IOException {
    // Note: 1-Wire autoconfigures within the filesystem
    logger.log(DeviceLogMessage.BUS_MANAGER_CONFIGURE_DEVICES);
    Map<String, Object> i2c = getConfig("i2c", config);
    if (!i2c.isEmpty()) {
      int bus = Integer.parseInt(i2c.get("bus").toString());
      i2cBusManager[bus].configureDevices(i2c);
    }
    Map<String, Object> spi = getConfig("spi", config);
    if (!spi.isEmpty()) {
      spiBusManager.configureDevices(spi);
    }
  }

  private Map<String, Object> getConfig(String bus, Map<String, Object> config) {
    Object object = config.get(bus);
    if (object instanceof Map) {
      return (Map<String, Object>) object;
    }
    return new LinkedHashMap<>();
  }

  public void close() {
    pi4j.shutdown();
    logger.log(DeviceLogMessage.BUS_MANAGER_SHUTDOWN);
  }

}
