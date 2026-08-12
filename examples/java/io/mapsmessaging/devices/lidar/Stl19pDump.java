/*
 *
 *  Copyright [ 2020 - 2024 ] Matthew Buckton
 *  Copyright [ 2024 - 2026 ] MapsMessaging B.V.
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

package io.mapsmessaging.devices.lidar;

import com.fazecast.jSerialComm.SerialPort;
import io.mapsmessaging.devices.serial.devices.sensors.SerialDevice;
import io.mapsmessaging.devices.serial.devices.sensors.sen0547.Stl19pController;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class Stl19pDump {

  private static final int BAUD_RATE = 230400;

  private Stl19pDump() {}

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      printUsage();
      return;
    }

    SerialPort serialPort = SerialPort.getCommPort(args[0]);
    serialPort.setComPortParameters(BAUD_RATE, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
    serialPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
    serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 100, 0);

    Stl19pController controller = new Stl19pController(new JSerialCommDevice(serialPort));
    Runtime.getRuntime().addShutdownHook(new Thread(controller::close, "stl19p-dump-shutdown"));

    System.err.printf("Reading SEN0547/STL-19P from %s at %d 8N1%n", serialPort.getSystemPortName(), BAUD_RATE);

    try {
      while (!Thread.currentThread().isInterrupted()) {
        byte[] event = controller.getDeviceState();
        System.out.println(new String(event, StandardCharsets.UTF_8));
      }
    } finally {
      controller.close();
    }
  }

  private static void printUsage() {
    System.err.println("Usage: Stl19pDump <serial-port>");
    System.err.println("Available serial ports:");
    for (SerialPort port : SerialPort.getCommPorts()) {
      System.err.printf("  %-20s %s%n", port.getSystemPortName(), port.getDescriptivePortName());
    }
  }

  private static final class JSerialCommDevice implements SerialDevice {

    private final SerialPort serialPort;

    private JSerialCommDevice(SerialPort serialPort) {
      this.serialPort = serialPort;
    }

    @Override
    public boolean isOpen() {
      return serialPort.isOpen();
    }

    @Override
    public boolean openPort() {
      return serialPort.openPort();
    }

    @Override
    public String getSystemPortName() {
      return serialPort.getSystemPortName();
    }

    @Override
    public void closePort() {
      serialPort.closePort();
    }

    @Override
    public int writeBytes(byte[] request, int length) {
      return serialPort.writeBytes(request, length);
    }

    @Override
    public int readBytes(byte[] buffer, int remaining) {
      return serialPort.readBytes(buffer, remaining);
    }
  }
}
