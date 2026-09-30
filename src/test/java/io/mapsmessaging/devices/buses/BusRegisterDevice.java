package io.mapsmessaging.devices.buses;

import io.mapsmessaging.devices.impl.AddressableDevice;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Register image at the transport boundary, including every byte of each outbound frame. */
final class BusRegisterDevice implements AddressableDevice {
  final byte[] registers = new byte[256];
  final List<byte[]> writes = new ArrayList<>();
  private final int address;
  BusRegisterDevice(int address) { this.address = address; }
  void seed(int register, int... values) {
    for (int i = 0; i < values.length; i++) registers[register + i] = (byte) values[i];
  }
  byte[] image(int register, int length) {
    return Arrays.copyOfRange(registers, register, register + length);
  }
  @Override public void close() { }
  @Override public int getBus() { return 1; }
  @Override public int getDevice() { return address; }
  @Override public int write(int value) { writes.add(new byte[]{(byte) value}); return 1; }
  @Override public int write(byte[] data, int offset, int length) {
    writes.add(Arrays.copyOfRange(data, offset, offset + length)); return length;
  }
  @Override public int writeRegister(int register, byte[] data) {
    byte[] frame = new byte[data.length + 1];
    frame[0] = (byte) register;
    System.arraycopy(data, 0, frame, 1, data.length);
    writes.add(frame);
    System.arraycopy(data, 0, registers, register, data.length);
    return data.length;
  }
  @Override public int readRegister(int register) { return registers[register] & 255; }
  @Override public int readRegister(int register, byte[] buffer, int offset, int length) {
    System.arraycopy(registers, register, buffer, offset, length); return length;
  }
  @Override public int read(byte[] buffer, int offset, int length) {
    throw new AssertionError("Unexpected unaddressed read");
  }
  @Override public int read() { throw new AssertionError("Unexpected unaddressed read"); }
}
