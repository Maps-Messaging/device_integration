package io.mapsmessaging.devices.i2c.devices.sensors;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.logging.LoggerFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Register image at the hardware boundary; command bits remain part of the address. */
final class SensorRegisterDevice implements AddressableDevice {
  private final byte[] image = new byte[256];
  final List<byte[]> commands = new ArrayList<>();
  final List<Integer> reads = new ArrayList<>();
  int failedRegister = -1;
  SensorRegisterDevice put(int address, int... bytes) {
    for (int i = 0; i < bytes.length; i++) image[address + i] = (byte) bytes[i];
    return this;
  }
  int value(int address) { return image[address] & 255; }
  I2CDevice bus() { return new RegisterBus(this); }
  @Override public void close() {}
  @Override public int getBus() { return 1; }
  @Override public int getDevice() { return 0x28; }
  @Override public int write(int value) { commands.add(new byte[]{(byte)value}); return 1; }
  @Override public int write(byte[] data, int offset, int length) {
    commands.add(Arrays.copyOfRange(data, offset, offset + length)); return length;
  }
  @Override public int writeRegister(int register, byte[] data) {
    System.arraycopy(data, 0, image, register, data.length); return data.length;
  }
  @Override public int read(byte[] data, int offset, int length) {
    throw new UnsupportedOperationException("Expected a register read");
  }
  @Override public int readRegister(int register) {
    reads.add(register); return register == failedRegister ? -1 : value(register);
  }
  @Override public int readRegister(int register, byte[] data, int offset, int length) {
    reads.add(register);
    if (register == failedRegister) return -1;
    System.arraycopy(image, register, data, offset, length); return length;
  }
  @Override public int read() { throw new UnsupportedOperationException("Expected a register read"); }

  private static final class RegisterBus extends I2CDevice {
    RegisterBus(AddressableDevice device) { super(device, LoggerFactory.getLogger(RegisterBus.class)); }
    @Override public boolean isConnected() { return true; }
    @Override public String getName() { return "register test bus"; }
    @Override public String getDescription() { return "in memory hardware boundary"; }
    @Override public DeviceType getType() { return DeviceType.SENSOR; }
    @Override public void delay(int milliseconds) { /* No physical conversion time in a register image. */ }
  }
}
