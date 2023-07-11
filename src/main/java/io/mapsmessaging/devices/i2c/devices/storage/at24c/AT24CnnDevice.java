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
    int offset = 0;
    while (offset < data.length) {
      int len = Math.min(32, (data.length - offset));
      writeBlock(address + offset, data, offset, len);
      offset += len;
      if (offset < data.length) {
        waitForReady();
      }
    }
  }

  private void waitForReady() {
    int count = 0;
    boolean cont = true;
    while (cont & count < 10) {
      try {
        cont = device.read() < 0;
      } catch (Exception e) {
        //ignore till we get the device to respond once more
      }
      delay(1);
      count++;
    }
  }

  // Read a byte array at the given address
  public byte[] readBytes(int address, int length) throws IOException {
    byte[] buffer = new byte[length];
    int offset = 0;
    while (offset < length) {
      int len = Math.min(32, (length - offset));
      int read = readBlock(address + offset, buffer, offset, len);
      if (read > 0) {
        offset += len;
      } else {
        throw new IOException("Failed to write device");
      }
    }
    return buffer;
  }

  // Read a byte array at the given address
  private int readBlock(int address, byte[] buffer, int offset, int len) throws IOException {
    byte[] writeBuffer = new byte[]{(byte) (address >> 8), (byte) (address & 0xFF)};
    write(writeBuffer, 0, writeBuffer.length);
    return read(buffer, offset, len);
  }

  // Write a byte array at the given address
  public void writeBlock(int address, byte[] data, int offset, int len) throws IOException {
    byte[] buffer = new byte[len + 2];
    buffer[0] = (byte) (address >> 8);
    buffer[1] = (byte) (address & 0xFF);
    System.arraycopy(data, offset, buffer, 2, len);
    write(buffer, 0, buffer.length);
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
