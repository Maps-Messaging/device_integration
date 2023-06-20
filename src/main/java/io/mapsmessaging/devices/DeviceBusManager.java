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
import com.pi4j.io.i2c.I2CProvider;
import io.mapsmessaging.devices.i2c.I2CBusManager;
import io.mapsmessaging.devices.interrupts.InterruptFactory;
import io.mapsmessaging.devices.oneWire.OneWireBusManager;
import lombok.Getter;

public class DeviceBusManager {

  private static final String[] PROVIDERS = {"pigpio-i2c", "linuxfs-i2c"};

  private static final DeviceBusManager instance = new DeviceBusManager();
  private final Context pi4j;
  private final I2CProvider i2cProvider;
  @Getter
  private final I2CBusManager i2cBusManager;
  @Getter
  private final OneWireBusManager oneWireBusManager;
  @Getter
  private final InterruptFactory interruptFactory;

  private DeviceBusManager() {


    pi4j = Pi4J.newAutoContext();
    i2cProvider = pi4j.provider(getProvider());
    i2cBusManager = new I2CBusManager(pi4j, i2cProvider);
    oneWireBusManager = new OneWireBusManager();
    interruptFactory = new InterruptFactory(pi4j);
  }

  public static DeviceBusManager getInstance() {
    return instance;
  }

  private static String getProvider() {
    String provider = System.getProperty("i2C-PROVIDER", PROVIDERS[0]).toLowerCase();
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

  public void close() {
    pi4j.shutdown();
  }
}
