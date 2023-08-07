package io.mapsmessaging.devices.impl;

public interface AddressableDevice {

  void close();

  int getBus();

  int write(int val);

  default int write(byte[] buffer) {
    return write(buffer, 0, buffer.length);
  }

  int write(byte[] buffer, int offset, int length);

  int writeRegister(int register, byte[] data);

  int read(byte[] buffer, int offset, int length);

  int readRegister(int register);

  int readRegister(int register, byte[] buffer, int offset, int length);

  int getDevice();

  int read();
}
  