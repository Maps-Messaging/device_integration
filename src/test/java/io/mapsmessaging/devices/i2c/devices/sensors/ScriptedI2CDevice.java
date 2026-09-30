package io.mapsmessaging.devices.i2c.devices.sensors;

import io.mapsmessaging.devices.impl.AddressableDevice;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

/** In-memory I2C device with ordered response frames and captured writes. */
class ScriptedI2CDevice implements AddressableDevice {
  private final int address;
  private final Queue<byte[]> responses = new ArrayDeque<>();
  private final List<byte[]> writes = new ArrayList<>();

  ScriptedI2CDevice(int address, byte[]... frames) {
    this.address = address;
    responses.addAll(Arrays.asList(frames));
  }

  List<byte[]> writes() {
    return writes;
  }

  @Override public void close() {
    // This in-memory device owns no external resources.
  }
  @Override public int getBus() { return 1; }
  @Override public int getDevice() { return address; }
  @Override public int write(int value) { writes.add(new byte[] {(byte) value}); return 1; }
  @Override public int write(byte[] data, int offset, int length) {
    writes.add(Arrays.copyOfRange(data, offset, offset + length));
    return length;
  }
  @Override public int writeRegister(int register, byte[] data) {
    writes.add(new byte[] {(byte) register});
    writes.add(data.clone());
    return data.length;
  }
  @Override public int read(byte[] buffer, int offset, int length) {
    byte[] frame = responses.poll();
    if (frame == null) return 0;
    int count = Math.min(length, frame.length);
    System.arraycopy(frame, 0, buffer, offset, count);
    return count;
  }
  @Override public int readRegister(int register) { return 0; }
  @Override public int readRegister(int register, byte[] buffer, int offset, int length) {
    return read(buffer, offset, length);
  }
  @Override public int read() { return 0; }
}
