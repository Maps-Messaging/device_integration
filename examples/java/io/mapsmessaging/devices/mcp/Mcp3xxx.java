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

package io.mapsmessaging.devices.mcp;

import io.mapsmessaging.devices.DeviceBusManager;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import io.mapsmessaging.devices.spi.SpiBusManager;
import io.mapsmessaging.devices.spi.SpiDeviceController;
import io.mapsmessaging.devices.spi.devices.mcp3y0x.Mcp3y0xController;

import java.util.LinkedHashMap;
import java.util.Map;

public class Mcp3xxx {

  public static void main(String[] args) throws InterruptedException {
    Map<String, String> deviceConfig = new LinkedHashMap<>();
    deviceConfig.put("spiBus", "0");
    deviceConfig.put("spiMode", "0");
    deviceConfig.put("spiChipSelect", "0");
    deviceConfig.put("resolution", "12");
    deviceConfig.put("channels", "8");

    SpiBusManager spiBusManager = DeviceBusManager.getInstance().getSpiBusManager();
    SpiDeviceController controller = spiBusManager.configureDevice("Mcp3y0x", deviceConfig);
    if (controller instanceof Mcp3y0xController) {
      Mcp3y0xController mcp3y0xController = (Mcp3y0xController) controller;
      while (true) {
        for (SensorReading<?> sensor : mcp3y0xController.getSensors()) {
          System.err.println(sensor.getName() + "> " + sensor.getValue().getResult());
        }
        Thread.sleep(100);
      }
    }
  }
}
