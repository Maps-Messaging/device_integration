package io.mapsmessaging.devices.buses;

import io.mapsmessaging.devices.serial.devices.sensors.SerialDevice;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Complete response frames from a UART, without replacing Modbus parsing. */
final class BusSerialPort implements SerialDevice {
  boolean open;
  boolean canOpen = true;
  int writeLimit = Integer.MAX_VALUE;
  int readResult = Integer.MAX_VALUE;
  byte[] response;
  final List<byte[]> requests = new ArrayList<>();
  BusSerialPort(byte... response) { this.response = response; }
  @Override public boolean isOpen() { return open; }
  @Override public boolean openPort() { open = canOpen; return open; }
  @Override public String getSystemPortName() { return "test-uart"; }
  @Override public void closePort() { open = false; }
  @Override public int writeBytes(byte[] request, int length) {
    requests.add(Arrays.copyOf(request, length)); return Math.min(writeLimit, length);
  }
  @Override public int readBytes(byte[] buffer, int remaining) {
    if (readResult != Integer.MAX_VALUE) return readResult;
    if (response == null) return 0;
    int count = Math.min(remaining, response.length);
    System.arraycopy(response, 0, buffer, 0, count);
    response = null;
    return count;
  }
}
