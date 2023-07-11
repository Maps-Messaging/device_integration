package io.mapsmessaging.devices.i2c.devices.storage.at24c;

import com.pi4j.io.i2c.I2C;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.logging.LoggerFactory;

import java.io.IOException;

public class AT24CnnDevice extends I2CDevice {

  protected AT24CnnDevice(I2C device) {
    super(device, LoggerFactory.getLogger(AT24CnnDevice.class));
  }

  // Read a single byte at the given address
  public byte readByte(int address) throws IOException {
    byte[] writeBuffer = new byte[] { (byte) (address >> 8), (byte) (address & 0xFF) };
    byte[] readBuffer = new byte[1];
    write(writeBuffer, 0, writeBuffer.length);
    read(readBuffer, 0, readBuffer.length);
    return readBuffer[0];
  }

  // Write a single byte at the given address
  public void writeByte(int address, byte data) throws IOException {
    byte[] buffer = new byte[] { (byte) (address >> 8), (byte) (address & 0xFF), data };
    write(buffer, 0, buffer.length);
  }

  // Write a byte array at the given address
  public void writeBytes(int address, byte[] data) throws IOException {
    byte[] buffer = new byte[data.length + 2];
    buffer[0] = (byte) (address >> 8);
    buffer[1] = (byte) (address & 0xFF);
    System.arraycopy(data, 0, buffer, 2, data.length);
    write(buffer, 0, buffer.length);
  }

  // Read a byte array at the given address
  public byte[] readBytes(int address, int length) throws IOException {
    byte[] writeBuffer = new byte[] { (byte) (address >> 8), (byte) (address & 0xFF) };
    byte[] readBuffer = new byte[length];
    write(writeBuffer, 0, writeBuffer.length);
    read(readBuffer, 0, readBuffer.length);
    return readBuffer;
  }

  @Override
  public String getName() {
    return "AT24C32/64";
  }

  @Override
  public String getDescription() {
    return "32Kb or 64Kb eeprom";
  }

  @Override
  public boolean isConnected() {
    return true;
  }
}
