/*
 *
 *  Copyright [ 2020 - 2024 ] Matthew Buckton
 *  Copyright [ 2024 - 2025 ] MapsMessaging B.V.
 *
 *  Licensed under the Apache License, Version 2.0 with the Commons Clause
 *  (the "License"); you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      https://commonsclause.com/
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */

package io.mapsmessaging.devices;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class DebugDeviceLoadTest {

  @Test
  void testLoad() throws IOException, InterruptedException {
    DeviceBusManager busManager = DeviceBusManager.getInstance();
    if(busManager.isAvailable()){
      System.err.println("It is available");
    }
    else{
      System.err.println("It is not available");
      Map<String, Object> config = new HashMap<>();
      Map<String, Object> debugConfig = new HashMap<>();
      debugConfig.put("type", "debug");
      debugConfig.put("enable", true);
      config.put("debug", debugConfig);
      busManager.configureDevices(config);
      busManager.getI2cBusManager()[2].scanForDevices(60000);
      Map<String, DeviceController> active = busManager.getI2cBusManager()[2].getActive();

      for(int x=0;x<10;x++) {
        for (Map.Entry<String, DeviceController> entry : active.entrySet()) {
          DeviceController controller = entry.getValue();
          if(x==0) System.err.println(controller.getSchema().pack());
          System.err.println(new String(controller.getDeviceState()));
        }
        Thread.sleep(2000);
      }
    }
  }
}
