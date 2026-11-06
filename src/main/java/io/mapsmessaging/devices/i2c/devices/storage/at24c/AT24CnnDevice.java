/*
 *
 *  Copyright [ 2020 - 2024 ] Matthew Buckton
 *  Copyright [ 2024 - 2026 ] MapsMessaging B.V.
 *
 *  Licensed under the Apache License, Version 2.0 with the Commons Clause
 *  (the "License"); you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      https://commonsclause.com/
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */

package io.mapsmessaging.devices.i2c.devices.storage.at24c;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.deviceinterfaces.Storage;
import io.mapsmessaging.devices.i2c.I2CDevice;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.logging.LoggerFactory;
import lombok.Getter;

import java.io.IOException;

@Getter
public class AT24CnnDevice extends I2CDevice implements Storage {
  private static final int DEFAULT_CAPACITY = 4096; // Safe lower bound; no destructive capacity probe.
  private final int memorySize;
  private int pageSize;

  protected AT24CnnDevice(AddressableDevice device) {
    this(device, DEFAULT_CAPACITY);
  }

  public AT24CnnDevice(AddressableDevice device, int capacityBytes) {
    super(device, LoggerFactory.getLogger(AT24CnnDevice.class));
    if (capacityBytes != 4096 && capacityBytes != 8192 && capacityBytes != 16384
        && capacityBytes != 32768 && capacityBytes != 65536) {
      throw new IllegalArgumentException("Unsupported AT24C EEPROM capacity in bytes: " + capacityBytes);
    }
    memorySize = capacityBytes;
    pageSize = capacityBytes == 65536 ? 128 : capacityBytes >= 16384 ? 64 : 32;
  }

  public void setPageSize(int pageSize) {
    int physicalPageSize = memorySize == 65536 ? 128 : memorySize >= 16384 ? 64 : 32;
    if (pageSize <= 0 || pageSize > physicalPageSize || physicalPageSize % pageSize != 0) {
      throw new IllegalArgumentException("EEPROM page size must divide the physical page size: " + physicalPageSize);
    }
    this.pageSize = pageSize;
  }

  private void checkRange(int address, int length) throws IOException {
    if (address < 0 || length < 0 || (long) address + length > memorySize) {
      throw new IOException("AT24C EEPROM access exceeds configured capacity of " + memorySize + " bytes");
    }
  }

  @Override
  public void writeBlock(int address, byte[] data) throws IOException {
    // Call the more flexible writeBlock method with full data length and start offset.
    writeBlock(address, data, data.length);
  }

  @Override
// Improved readBlock method for efficiency
  public byte[] readBlock(int address, int length) throws IOException {
    checkRange(address, length);
    byte[] buffer = new byte[length];
    int offset = 0;
    while (offset < length) {
      int len = Math.min(pageSize, (length - offset));
      int read = readChunk(address + offset, buffer, offset, len);
      if (read > 0 && read <= len) {
        offset += read;
      } else {
        throw new IOException("Failed to read device");
      }
    }
    return buffer;
  }

  // Read a chunk of data at the given address into the specified buffer
  private int readChunk(int address, byte[] buffer, int offset, int len) throws IOException {
    byte[] writeBuffer = {(byte) (address >> 8), (byte) (address & 0xFF)};
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

  // Make this the primary write method with detailed parameters
  private void writeBlock(int address, byte[] data, int len) throws IOException {
    checkRange(address, len);
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
      delay(5); // AT24C write-cycle maximum; do not consume data bytes as an ACK probe.

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
