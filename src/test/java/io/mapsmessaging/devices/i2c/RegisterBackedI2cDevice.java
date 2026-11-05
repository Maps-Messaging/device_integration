package io.mapsmessaging.devices.i2c;

import io.mapsmessaging.devices.impl.AddressableDevice;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** In-memory byte-addressed register device for deterministic I2C command tests. */
public class RegisterBackedI2cDevice implements AddressableDevice {
  private final int address;
  private final byte[] registers = new byte[256];
  private final List<byte[]> writes = new ArrayList<>();

  public RegisterBackedI2cDevice(int address) {
    this.address = address;
  }

  public void setRegister(int register, int value) {
    registers[register & 0xff] = (byte) value;
  }

  public int register(int register) {
    return registers[register & 0xff] & 0xff;
  }

  public List<byte[]> writes() {
    return writes;
  }

  @Override
  public void close() { }

  @Override
  public int getBus() { return 1; }

  @Override
  public int getDevice() { return address; }

  @Override
  public int write(int value) {
    writes.add(new byte[] {(byte) value});
    return 1;
  }

  @Override
  public int write(byte[] data, int offset, int length) {
    writes.add(Arrays.copyOfRange(data, offset, offset + length));
    return length;
  }

  @Override
  public int writeRegister(int register, byte[] data) {
    writes.add(new byte[] {(byte) register, data.length > 0 ? data[0] : 0});
    System.arraycopy(data, 0, registers, register & 0xff, data.length);
    return data.length;
  }

  @Override
  public int read(byte[] buffer, int offset, int length) {
    return 0;
  }

  @Override
  public int read() { return 0; }

  @Override
  public int readRegister(int register) {
    return registers[register & 0xff] & 0xff;
  }

  @Override
  public int readRegister(int register, byte[] buffer, int offset, int length) {
    System.arraycopy(registers, register & 0xff, buffer, offset, length);
    return length;
  }
}
