package io.mapsmessaging.devices.i2c.devices.storage.at24c;

import io.mapsmessaging.devices.deviceinterfaces.Storage;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.logging.LoggerFactory;
import lombok.Getter;

import java.io.IOException;

@Getter
public class AT24CnnDevice extends I2CDevice implements Storage {

  private final int memorySize;

  protected AT24CnnDevice(AddressableDevice device) {
    super(device, LoggerFactory.getLogger(AT24CnnDevice.class));
    int size = 8192;
    try {
      int val = readByte(8191);
      if (val < 0) {
        size = 4096;
      }
    } catch (IOException e) {
      size = 4096;
    }
    memorySize = size;
  }

  @Override
  public void writeBlock(int address, byte[] data) throws IOException {
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

  @Override
  public byte[] readBlock(int address, int length) throws IOException {
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

  @Override
  public String getName() {
    if (memorySize == 8192) {
      return "AT24C64";
    }
    return "AT24C32";
  }

  @Override
  public String getDescription() {
    if (memorySize == 8192) {
      return "64Kb eeprom";
    }
    return "32Kb eeprom";
  }

  @Override
  public boolean isConnected() {
    return true;
  }

  private void waitForReady() {
    int count = 0;
    boolean cont = true;
    while (cont && count < 10) {
      try {
        cont = device.read() < 0;
      } catch (Exception e) {
        //ignore till we get the device to respond once more
      }
      delay(1);
      count++;
    }
  }

  // Read a single byte at the given address
  private byte readByte(int address) throws IOException {
    byte[] writeBuffer = new byte[]{(byte) (address >> 8), (byte) (address & 0xFF)};
    byte[] readBuffer = new byte[1];
    write(writeBuffer, 0, writeBuffer.length);
    int val = read(readBuffer, 0, readBuffer.length);
    if (val < 0) return (byte) -1;
    return readBuffer[0];
  }


  // Read a byte array at the given address
  private int readBlock(int address, byte[] buffer, int offset, int len) throws IOException {
    byte[] writeBuffer = new byte[]{(byte) (address >> 8), (byte) (address & 0xFF)};
    write(writeBuffer, 0, writeBuffer.length);
    return read(buffer, offset, len);
  }

  // Write a byte array at the given address
  private void writeBlock(int address, byte[] data, int offset, int len) throws IOException {
    byte[] buffer = new byte[len + 2];
    buffer[0] = (byte) (address >> 8);
    buffer[1] = (byte) (address & 0xFF);
    System.arraycopy(data, offset, buffer, 2, len);
    write(buffer, 0, buffer.length);
  }

}
