/*
 *    Copyright [ 2020 - 2024 ] Matthew Buckton
 *    Copyright [ 2024 - 2025 ] MapsMessaging B.V.
 *
 *    Licensed under the Apache License, Version 2.0 with the Commons Clause
 *    (the "License"); you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at:
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *        https://commonsclause.com/
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License
 */

package io.mapsmessaging.devices.weather;

import com.fazecast.jSerialComm.SerialPort;
import io.mapsmessaging.devices.DeviceBusManager;
import io.mapsmessaging.devices.serial.SerialDeviceController;
import io.mapsmessaging.devices.serial.devices.sensors.sen0640.Sen0640Controller;
import io.mapsmessaging.devices.serial.devices.sensors.sen0657.Sen0657Controller;

import java.io.IOException;

public class WeatherMonitor {

  private static void setupSerial(SerialPort serialPort) {
    serialPort.setComPortParameters(4800, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
    serialPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
    serialPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
    int timeoutMillis = (int) Math.max(1L, 100);
    serialPort.setComPortTimeouts(
        SerialPort.TIMEOUT_READ_BLOCKING,
        timeoutMillis,
        0
    );
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    SerialPort serialPort0 = SerialPort.getCommPort("/dev/ttyAMA0");
    SerialPort serialPort1 = SerialPort.getCommPort("/dev/ttyAMA1");

    setupSerial(serialPort0);
    setupSerial(serialPort1);

    SerialDeviceController solarCfg = DeviceBusManager.getInstance().getSerialBusManager().getDevice("SEN0640");
    SerialDeviceController weatherCfg = DeviceBusManager.getInstance().getSerialBusManager().getDevice("SEN0657");
    Sen0657Controller weather = (Sen0657Controller) weatherCfg.mount(new Serial(serialPort0));
    Sen0640Controller solar = (Sen0640Controller) solarCfg.mount(new Serial(serialPort1));

    int count = 3000;
    while (count > 0) {
      Thread.sleep(1000);
      count = count - 1;
      try {
        System.err.println(new String(weather.getDeviceState()));
        System.err.println(new String(solar.getDeviceState()));
      } catch (IOException e) {
        System.err.println("Sensor read failed: " + e.getMessage());
      }
    }

  }
}
