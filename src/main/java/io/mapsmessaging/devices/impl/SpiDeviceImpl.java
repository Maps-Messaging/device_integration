package io.mapsmessaging.devices.impl;

import com.pi4j.io.spi.Spi;

public class SpiDeviceImpl implements AddressableDevice {

  private final Spi spi;

  public SpiDeviceImpl(Spi spi) {
    this.spi = spi;
  }

  @Override
  public void close() {
    spi.close();
  }

  @Override
  public int getBus() {
    return 0;
  }

  @Override
  public int write(int val) {
    return spi.write(val);
  }

  @Override
  public int write(byte[] buffer, int offset, int length) {
    return 0;
  }

  @Override
  public int writeRegister(int register, byte[] data) {
    return 0;
  }

  @Override
  public int read(byte[] buffer, int offset, int length) {
    return spi.read(buffer, offset, length);
  }

  @Override
  public int readRegister(int register) {
    return spi.read();
  }

  @Override
  public int readRegister(int register, byte[] buffer, int offset, int length) {
    return 0;
  }

  @Override
  public int getDevice() {
    return 0;
  }

  @Override
  public int read() {
    return 0;
  }
}
