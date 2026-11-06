package io.mapsmessaging.devices.i2c.devices.storage.at24c;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.mapsmessaging.devices.impl.AddressableDevice;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class At24CnnDatasheetTest {

  @Test
  void mountingDoesNotWriteToEeprom() {
    MemoryBus bus = new MemoryBus(4096, 32);
    new AT24CnnDevice(bus);

    assertEquals(0, bus.writeCount);
  }

  @Test
  void configuredCapacityDeterminesPhysicalPageSize() throws Exception {
    MemoryBus bus = new MemoryBus(16384, 64);
    AT24CnnDevice memory = new AT24CnnDevice(bus, 16384);
    byte[] data = new byte[10];
    for (int index = 0; index < data.length; index++) {
      data[index] = (byte) (index + 1);
    }

    assertEquals(64, memory.getPageSize());
    memory.writeBlock(60, data);
    assertEquals(2, bus.writeCount);
    assertArrayEquals(data, memory.readBlock(60, 10));
  }

  @Test
  void partialReadsFillRequestedLength() throws Exception {
    MemoryBus bus = new MemoryBus(4096, 32);
    AT24CnnDevice memory = new AT24CnnDevice(bus);
    byte[] data = {5, 6, 7, 8, 9};
    memory.writeBlock(100, data);

    assertArrayEquals(data, memory.readBlock(100, data.length));
  }

  @Test
  void rejectOutOfCapacityOperationsBeforeBusAccess() {
    MemoryBus bus = new MemoryBus(4096, 32);
    AT24CnnDevice memory = new AT24CnnDevice(bus);
    assertThrows(IOException.class, () -> memory.readBlock(4095, 2));
    assertThrows(IOException.class, () -> memory.writeBlock(-1, new byte[] {1}));
    assertThrows(IOException.class, () -> memory.writeBlock(4096, new byte[] {1}));
    assertEquals(0, bus.writeCount);
  }

  private static final class MemoryBus implements AddressableDevice {
    private final byte[] memory;
    private final int physicalPageSize;
    private int pointer;
    private int writeCount;

    MemoryBus(int size, int pageSize) {
      memory = new byte[size];
      physicalPageSize = pageSize;
    }

    @Override public void close() { }
    @Override public int getBus() { return 1; }
    @Override public int getDevice() { return 0x50; }
    @Override public int write(int value) { return 1; }
    @Override public int write(byte[] data, int offset, int length) {
      if (length < 2) return -1;
      pointer = ((data[offset] & 0xff) << 8) | (data[offset + 1] & 0xff);
      if (length > 2) {
        writeCount++;
        int pageStart = pointer - (pointer % physicalPageSize);
        for (int i = 2; i < length; i++) {
          int location = pageStart + ((pointer % physicalPageSize + i - 2) % physicalPageSize);
          memory[location] = data[offset + i];
        }
      }
      return length;
    }
    @Override public int writeRegister(int register, byte[] data) { return -1; }
    @Override public int read(byte[] buffer, int offset, int length) {
      int count = Math.min(2, Math.min(length, memory.length - pointer));
      System.arraycopy(memory, pointer, buffer, offset, count);
      pointer += count;
      return count;
    }
    @Override public int readRegister(int register) { return -1; }
    @Override public int readRegister(int register, byte[] buffer, int offset, int length) { return -1; }
    @Override public int read() { return 0; }
  }
}
