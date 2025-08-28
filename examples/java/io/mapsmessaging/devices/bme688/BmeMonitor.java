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

package io.mapsmessaging.devices.bme688;

import io.mapsmessaging.devices.DeviceBusManager;
import io.mapsmessaging.devices.i2c.I2CBusManager;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.I2CDeviceScheduler;
import io.mapsmessaging.devices.i2c.devices.sensors.bme688.BME688Sensor;
import io.mapsmessaging.devices.sensorreadings.ComputationResult;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import lombok.SneakyThrows;

import java.io.IOException;

import static io.mapsmessaging.devices.util.Constants.roundFloatToString;

public class BmeMonitor implements Runnable {

  private final BME688Sensor device;

  public BmeMonitor(BME688Sensor device) {
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
    I2CDeviceController deviceController = i2cBusManagers[bus].configureDevice(0x77, "BME688");
    if (deviceController != null) {
      System.err.println(new String(deviceController.getDeviceConfiguration()));
      I2CDevice sensor = deviceController.getDevice();
      if (sensor instanceof BME688Sensor) {
        new BmeMonitor((BME688Sensor) sensor);
      }
    }
  }

  @SneakyThrows
  public void run() {
    SensorReading<?> gas = null;
    SensorReading<?> temp = null;
    SensorReading<?> humidity = null;
    SensorReading<?> pressure = null;

    for (SensorReading<?> val : device.getReadings()) {
      switch (val.getName()) {
        case "gas":
          gas = val;
          break;
        case "humidity":
          humidity = val;
          break;
        case "pressure":
          pressure = val;
          break;
        case "temperature":
          temp = val;
          break;
      }
    }
    long stop = System.currentTimeMillis() + 120_000;

    while (gas != null && humidity != null && temp != null && pressure != null && stop > System.currentTimeMillis()) {
      synchronized (I2CDeviceScheduler.getI2cBusLock()) {
        ComputationResult<Float> tempResult = (ComputationResult<Float>) temp.getValue();
        ComputationResult<Float> gasResult = (ComputationResult<Float>) gas.getValue();
        ComputationResult<Float> humResult = (ComputationResult<Float>) humidity.getValue();
        ComputationResult<Float> preResult = (ComputationResult<Float>) pressure.getValue();
        if (!gasResult.hasError()) {
          float gasV = gasResult.getResult();
          float tRes = tempResult.getResult();
           String pre = roundFloatToString(gasV, 2);
          String tmp = roundFloatToString(tRes, 1);
          String dis = roundFloatToString(humResult.getResult(), 1);
          String pres = roundFloatToString(preResult.getResult(), 1);
          System.err.println(pre + " Ohms\t" + tmp + " C\t" + dis + "%"+"\t"+pres+"hPa");
          Thread.sleep(1000);
        }
      }
    }
  }
}
