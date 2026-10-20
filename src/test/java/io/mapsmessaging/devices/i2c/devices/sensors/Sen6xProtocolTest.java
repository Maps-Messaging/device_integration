package io.mapsmessaging.devices.i2c.devices.sensors;

import static org.junit.jupiter.api.Assertions.*;

import io.mapsmessaging.devices.i2c.devices.sensors.sen6x.Sen6xCommandHelper;
import io.mapsmessaging.devices.i2c.devices.sensors.sen6x.commands.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class Sen6xProtocolTest {

  @Test
  void commandIdHasNoAdditionalCrc() throws Exception {
    ScriptedI2CDevice bus = new ScriptedI2CDevice(0x6b);
    helper(bus).sendCommand(0x5607);
    assertArrayEquals(new byte[] {0x56, 0x07}, bus.writes().get(0));
  }

  @Test
  void writePayloadAddsCrcOnlyAfterDataWords() {
    ScriptedI2CDevice bus = new ScriptedI2CDevice(0x6b);
    helper(bus).writeWithCRC(new byte[] {0x60, (byte) 0xd0, (byte) 0xbe, (byte) 0xef});
    assertArrayEquals(new byte[] {0x60, (byte) 0xd0, (byte) 0xbe, (byte) 0xef, (byte) 0x92}, bus.writes().get(0));
  }

  @Test
  void receivedFfCrcIsValidated() {
    ScriptedI2CDevice bus = new ScriptedI2CDevice(0x6b, new byte[] {0, 1, (byte) 0xff});
    assertThrows(IOException.class, () -> helper(bus).requestResponse(0x0202, 3, 0));
  }

  @Test
  void datasheetCrcVectorDecodes() throws Exception {
    ScriptedI2CDevice bus = new ScriptedI2CDevice(0x6b, new byte[] {(byte) 0xbe, (byte) 0xef, (byte) 0x92});
    assertArrayEquals(new byte[] {(byte) 0xbe, (byte) 0xef}, helper(bus).requestResponse(0x0300, 3, 0));
  }

  @Test
  void sendOnlyCommandDoesNotPerformRead() throws Exception {
    ScriptedI2CDevice bus = new ScriptedI2CDevice(0x6b) {
      @Override public int read(byte[] data, int offset, int length) {
        fail("Send-only command must not read");
        return -1;
      }
    };
    assertEquals(0, helper(bus).requestResponse(0x0021, 0, 0).length);
  }

  @Test
  void responseLengthMustContainCompleteWordsAndCrc() {
    assertThrows(IllegalArgumentException.class,
        () -> helper(new ScriptedI2CDevice(0x6b, new byte[4])).requestResponse(0x0300, 4, 0));
  }

  @Test
  void partialCommandWriteFailsBeforeRead() {
    ScriptedI2CDevice bus = new ScriptedI2CDevice(0x6b, frame(1)) {
      @Override public int write(byte[] data, int offset, int length) { return 1; }
    };
    assertThrows(IOException.class, () -> helper(bus).requestResponse(0x0202, 3, 0));
  }

  @Test
  void interruptedDelayDoesNotReadSensor() {
    ScriptedI2CDevice bus = new ScriptedI2CDevice(0x6b, frame(1));
    Sen6xCommandHelper helper = new Sen6xCommandHelper(bus) {
      @Override public void delay(int millis) { Thread.currentThread().interrupt(); }
    };
    try {
      assertThrows(IOException.class, () -> helper.requestResponse(0x0202, 3));
      assertTrue(Thread.currentThread().isInterrupted());
    } finally {
      Thread.interrupted();
    }
  }

  @Test
  void statusReadsAllThirtyTwoBits() throws Exception {
    ScriptedI2CDevice bus = new ScriptedI2CDevice(0x6b, frame(0x20, 0x10));
    var status = new GetDeviceStatusCommand(helper(bus)).execute();
    assertTrue(status.isSpeedWarning());
    assertTrue(status.isFanError());
    assertFalse(status.isRhtError());
  }

  @Test
  void versionIsBinaryMajorAndMinor() throws Exception {
    ScriptedI2CDevice bus = new ScriptedI2CDevice(0x6b, frame(0x0402), frame(0x0402));
    assertEquals("4.2", new GetVersionCommand(helper(bus)).execute());
    assertEquals("4.2", new GetFirmwareVersionCommand(helper(bus)).execute());
    assertArrayEquals(new byte[] {(byte) 0xd1, 0}, bus.writes().get(0));
  }

  @Test
  void clearStatusConsumesAndValidatesResponse() {
    ScriptedI2CDevice bus = new ScriptedI2CDevice(0x6b, new byte[] {0, 0, 0, 0, 0, 0});
    assertThrows(IOException.class, () -> new ClearDeviceStatusCommand(helper(bus)).execute());
  }

  @Test
  void unsupportedFanIntervalCommandsNeverTouchTheBus() {
    ScriptedI2CDevice bus = new ScriptedI2CDevice(0x6b, frame(0));
    assertThrows(UnsupportedOperationException.class, () -> new GetFanCleaningIntervalCommand(helper(bus)).execute());
    assertThrows(UnsupportedOperationException.class, () -> new SetFanCleaningIntervalCommand(helper(bus)).execute());
    assertTrue(bus.writes().isEmpty());
  }

  @Test
  void commandDelaysMatchDatasheet() throws Exception {
    List<Integer> delays = new ArrayList<>();
    Sen6xCommandHelper helper = new Sen6xCommandHelper(new ScriptedI2CDevice(0x6b, frame(1))) {
      @Override public void delay(int millis) { delays.add(millis); }
    };
    new StartMeasurementCommand(helper).execute();
    new StopMeasurementCommand(helper).execute();
    new SoftResetCommand(helper).execute();
    new StartFanCleaningCommand(helper).execute();
    new GetDataReadyFlagCommand(helper).execute();
    assertEquals(List.of(50, 1400, 1200, 10000, 20), delays);
  }

  static Sen6xCommandHelper helper(ScriptedI2CDevice bus) {
    return new Sen6xCommandHelper(bus) {
      @Override public void delay(int millis) { }
    };
  }

  static byte[] frame(int... words) {
    byte[] frame = new byte[words.length * 3];
    for (int index = 0; index < words.length; index++) {
      int word = words[index];
      frame[index * 3] = (byte) (word >>> 8);
      frame[index * 3 + 1] = (byte) word;
      int crc = 0xff;
      for (int value : new int[] {(word >>> 8) & 0xff, word & 0xff}) {
        crc ^= value;
        for (int bit = 0; bit < 8; bit++) {
          crc = ((crc & 0x80) != 0 ? (crc << 1) ^ 0x31 : crc << 1) & 0xff;
        }
      }
      frame[index * 3 + 2] = (byte) crc;
    }
    return frame;
  }
}
