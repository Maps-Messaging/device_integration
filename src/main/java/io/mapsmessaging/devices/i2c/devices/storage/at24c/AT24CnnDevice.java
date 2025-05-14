/*
 *
 *  Copyright [ 2020 - 2024 ] [Matthew Buckton]
 *  Copyright [ 2024 - 2025.  ] [Maps Messaging B.V.]
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package io.mapsmessaging.devices.i2c.devices.storage.at24c;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.deviceinterfaces.Storage;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.logging.LoggerFactory;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

@Getter
public class AT24CnnDevice extends I2CDevice implements Storage {
  private static final int[] possibleSizes = new int[]{65536, 32768, 16384, 8192, 4096};
  private static final int PAGE_SIZE_AT24C32 = 32; // 32 bytes for AT24C32
  private static final int PAGE_SIZE_AT24C512 = 128; // 128 bytes for AT24C512

  private final int memorySize;
  @Setter
  private int pageSize;

  protected AT24CnnDevice(AddressableDevice device) {
    super(device, LoggerFactory.getLogger(AT24CnnDevice.class));
    int size = 4096;

    for (int testSize : possibleSizes) {
      try {
        byte originalValue = readByte(testSize - 1);
        writeByte(testSize - 1, (byte) (originalValue + 1)); // Temporarily modify the value.
        byte modifiedValue = readByte(testSize - 1);
        writeByte(testSize - 1, originalValue); // Restore original value.

        // Check if the write and read back was successful.
        if (modifiedValue == (byte) (originalValue + 1)) {
          size = testSize;
          break;
        }
      } catch (IOException e) {
        // Failed to read/write at this address, likely out of bounds.
      }
    }
    memorySize = size;
    pageSize = memorySize > 8192 ? PAGE_SIZE_AT24C512 : PAGE_SIZE_AT24C32;
  }

  @Override
  public void writeBlock(int address, byte[] data) throws IOException {
    // Call the more flexible writeBlock method with full data length and start offset.
    writeBlock(address, data, data.length);
  }

  @Override
// Improved readBlock method for efficiency
  public byte[] readBlock(int address, int length) throws IOException {
    byte[] buffer = new byte[length];
    int offset = 0;
    while (offset < length) {
      int len = Math.min(pageSize, (length - offset));
      int read = readChunk(address + offset, buffer, offset, len);
      if (read > 0) {
        offset += len;
      } else {
        throw new IOException("Failed to read device");
      }
    }
    return buffer;
  }

  // Read a chunk of data at the given address into the specified buffer
  private int readChunk(int address, byte[] buffer, int offset, int len) throws IOException {
    byte[] writeBuffer = new byte[]{(byte) (address >> 8), (byte) (address & 0xFF)};
    write(writeBuffer, 0, writeBuffer.length); // Set the EEPROM's address pointer
    return read(buffer, offset, len); // Read the chunk into the buffer
  }


  @Override
  public String getName() {
    switch (memorySize) {
      case 4096:
        return "AT24C32";
      case 8192:
        return "AT24C64";
      case 16384:
        return "AT24C128";
      case 32768:
        return "AT24C256";
      case 65536:
        return "AT24C512";
      default:
        return "Unknown";
    }
  }

  @Override
  public String getDescription() {
    switch (memorySize) {
      case 4096:
        return "32Kb eeprom";
      case 8192:
        return "64Kb eeprom";
      case 16384:
        return "128Kb eeprom";
      case 32768:
        return "256Kb eeprom";
      case 65536:
        return "512Kb eeprom";
      default:
        return "Unknown size";
    }
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

  // Read a single byte at the given address
  private void writeByte(int address, byte val) throws IOException {
    byte[] writeBuffer = new byte[]{(byte) (address >> 8), (byte) (address & 0xFF), val};
    write(writeBuffer, 0, writeBuffer.length);
  }


  // Make this the primary write method with detailed parameters
  private void writeBlock(int address, byte[] data, int len) throws IOException {
    int bytesToWrite = len;
    int currentOffset = 0;
    int currentAddress = address;

    while (bytesToWrite > 0) {
      int pageOffset = currentAddress % pageSize;
      int bytesToEndOfPage = pageSize - pageOffset;
      int writeLength = Math.min(bytesToEndOfPage, bytesToWrite);

      byte[] buffer = new byte[writeLength + 2];
      buffer[0] = (byte) (currentAddress >> 8);
      buffer[1] = (byte) (currentAddress & 0xFF);
      System.arraycopy(data, currentOffset, buffer, 2, writeLength);

      write(buffer, 0, buffer.length);
      waitForReady();

      bytesToWrite -= writeLength;
      currentOffset += writeLength;
      currentAddress += writeLength;
    }
  }


  @Override
  public DeviceType getType() {
    return DeviceType.STORAGE;
  }

}
