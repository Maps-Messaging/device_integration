package io.mapsmessaging.devices.i2c.devices.sensors;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.logging.LoggerFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Gravity command/reply transport: sending a request must not overwrite the prepared response. */
final class GravityTestDevice implements AddressableDevice {
  final List<byte[]> commands = new ArrayList<>();
  private byte[] response;

  GravityTestDevice reply(int... values) {
    response = new byte[values.length];
    for (int index = 0; index < values.length; index++) {
      response[index] = (byte) values[index];
    }
    return this;
  }

  I2CDevice bus() {
    return new RegisterBus(this);
  }

  @Override public void close() { }
  @Override public int getBus() { return 1; }
  @Override public int getDevice() { return 0x76; }
  @Override public int write(int value) { throw new AssertionError("Expected a command frame"); }
  @Override public int write(byte[] data, int offset, int length) {
    commands.add(Arrays.copyOfRange(data, offset, offset + length));
    return length;
  }
  @Override public int writeRegister(int register, byte[] data) {
    byte[] wireFrame = new byte[data.length + 1];
    wireFrame[0] = (byte) register;
    System.arraycopy(data, 0, wireFrame, 1, data.length);
    commands.add(wireFrame);
    return data.length;
  }
  @Override public int readRegister(int register, byte[] data, int offset, int length) {
    if (register != 0) throw new AssertionError("Expected Gravity response register 0");
    if (response == null) throw new AssertionError("No Gravity response prepared");
    int count = Math.min(length, response.length);
    System.arraycopy(response, 0, data, offset, count);
    return count;
  }
  @Override public int readRegister(int register) { throw new AssertionError("Expected a response frame read"); }
  @Override public int read(byte[] data, int offset, int length) { throw new AssertionError("Expected response register 0"); }
  @Override public int read() { throw new AssertionError("Expected a response frame read"); }

  private static final class RegisterBus extends I2CDevice {
    RegisterBus(AddressableDevice device) {
      super(device, LoggerFactory.getLogger(RegisterBus.class));
    }
    @Override public boolean isConnected() { return true; }
    @Override public String getName() { return "Gravity test bus"; }
    @Override public String getDescription() { return "independent command and response buffers"; }
    @Override public DeviceType getType() { return DeviceType.SENSOR; }
    @Override public void delay(int milliseconds) { /* No hardware conversion time in this fixture. */ }
  }
}
