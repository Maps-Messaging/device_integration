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

package io.mapsmessaging.devices.pmsa03;

import io.mapsmessaging.devices.DeviceBusManager;
import io.mapsmessaging.devices.i2c.I2CBusManager;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.I2CDeviceScheduler;
import io.mapsmessaging.devices.i2c.devices.sensors.pmsa003i.Pmsa003iSensor;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import lombok.SneakyThrows;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Pmsa003Publisher implements Runnable {

  private final Pmsa003iSensor device;

  public Pmsa003Publisher(Pmsa003iSensor device) {
    this.device = device;
    Thread t = new Thread(this);
    t.start();
  }

  public static void main(String[] args) throws IOException {
    I2CBusManager[] i2cBusManagers = DeviceBusManager.getInstance().getI2cBusManager();
    int bus = 1;
    if (args.length > 0) {
      bus = Integer.parseInt(args[0]);
    }
    // Configure and mount a device on address 0x5D as a LPS25 pressure & temperature
    I2CDeviceController deviceController = i2cBusManagers[bus].configureDevice(0x12, "PMSA003I");
    if (deviceController != null) {
      I2CDevice sensor = deviceController.getDevice();
      if (sensor instanceof Pmsa003iSensor) {
        new Pmsa003Publisher((Pmsa003iSensor) sensor);
      }
    }
  }

  @SneakyThrows
  public void run() {
    List<SensorReading<?>> readings = device.getReadings();
    while (!readings.isEmpty()) {
      Map<String, Object> values = new HashMap<>();
      synchronized (I2CDeviceScheduler.getI2cBusLock()) {
        for (SensorReading<?> reading : readings) {
          Map<String, Object> sensor = new HashMap<>();
          sensor.put("unit", reading.getUnit());
          sensor.put("value", reading.getValue().getResult());
          values.put(reading.getName(), sensor);
        }
      }
      JSONObject jsonObject = new JSONObject(values);
      System.err.println(jsonObject.toString(2));
      Thread.sleep(30000);
    }
  }
}
