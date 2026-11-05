package io.mapsmessaging.devices.i2c.devices.sensors;

import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2c.devices.sensors.pn532.Pn532Controller;
import io.mapsmessaging.devices.i2c.devices.sensors.pn532.Pn532Sensor;
import io.mapsmessaging.devices.impl.AddressableDevice;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Pn532LifecycleTest {
  @Test
  void discoveryProbesFirmwareWithoutSamOrRfConfiguration() {
    Device device = new Device();
    device.exchange(0x03, 0x32, 1, 6, 7);
    Pn532Controller controller = new Pn532Controller();
    assertTrue(controller.canDetect());
    assertTrue(controller.detect(device));
    assertEquals(1, device.writes.size());
    assertArrayEquals(new byte[]{0, 0, (byte) 0xff, 2, (byte) 0xfe, (byte) 0xd4, 2, 0x2a, 0}, device.writes.getFirst());
  }

  @Test
  void discoveryRejectsWrongChipMalformedFramesAndIoFailure() {
    Pn532Controller controller = new Pn532Controller();
    Device wrongChip = new Device();
    wrongChip.exchange(0x03, 0x33, 1, 6, 7);
    assertFalse(controller.detect(wrongChip));
    for (int index : new int[]{3, 5, 6, 7, 12, 13}) {
      Device corrupt = new Device();
      corrupt.frames.add(MotionStartupTestDevice.ack());
      byte[] frame = MotionStartupTestDevice.frame(0x03, 0x32, 1, 6, 7);
      frame[index] ^= 1;
      corrupt.frames.add(frame);
      assertFalse(controller.detect(corrupt));
    }
    for (byte[] ack : new byte[][]{{1, 0, 0}, {1, 0, 0, (byte) 0xff, 1, (byte) 0xff, 0}}) {
      Device invalidAck = new Device();
      invalidAck.frames.add(ack);
      assertFalse(controller.detect(invalidAck));
    }
    Device shortFrame = new Device();
    shortFrame.frames.add(MotionStartupTestDevice.ack());
    shortFrame.frames.add(new byte[]{1, 0, 0, (byte) 0xff, 6, (byte) 0xfa, (byte) 0xd5, 3});
    assertFalse(controller.detect(shortFrame));
    assertFalse(controller.detect(new Device()));
  }

  @Test
  void mountRetainsAddressAndConfiguresFinitePassiveRetries() throws Exception {
    Device device = new Device().startup();
    I2CDeviceController controller = new Pn532Controller().mount(device);
    assertEquals(0x24, controller.getMountedAddress());
    assertEquals(3, device.writes.size());
    assertArrayEquals(new byte[]{0, 0, (byte) 0xff, 6, (byte) 0xfa, (byte) 0xd4,
        0x32, 5, (byte) 0xff, 1, 0, (byte) 0xf5, 0}, device.writes.get(2));
  }

  @Test
  void repeatedNoTagPollsAndTagRemovalReturnNormalAbsence() {
    Device device = new Device().startup();
    device.exchange(0x4b, 0);
    device.exchange(0x4b, 0);
    device.tag();
    device.exchange(0x4b, 0);
    Pn532Sensor sensor = new Pn532Sensor(device);
    assertFalse(sensor.isTagPresent());
    assertFalse(sensor.isTagPresent());
    assertTrue(sensor.isTagPresent());
    assertEquals("DEADBEEF", sensor.getLastUid());
    assertFalse(sensor.isTagPresent());
    assertEquals("", sensor.getLastUid());
  }

  @Test
  void liveConnectionCheckFailsAfterDisconnectAndReinitializesOnReturn() {
    Device device = new Device().startup();
    Pn532Sensor sensor = new Pn532Sensor(device);
    device.exchange(0x03, 0x32, 1, 6, 7);
    assertTrue(sensor.isConnected());
    assertFalse(sensor.isConnected());
    device.startup();
    assertTrue(sensor.isConnected());
    assertTrue(device.frames.isEmpty());
  }

  @Test
  void cardInformationReinitializesDisconnectedReaderBeforeSearching() throws Exception {
    Device device = new Device().startup();
    Pn532Sensor sensor = new Pn532Sensor(device);
    assertFalse(sensor.isConnected());
    device.startup();
    device.exchange(0x4b, 0);
    assertEquals(0, sensor.getInformation().uidLength);
    assertTrue(device.frames.isEmpty());
  }

  @Test
  void busyTimeoutAbortsOutstandingCommandAndNextPollRecovers() throws Exception {
    Device device = new Device().startup();
    Pn532Sensor sensor = new Pn532Sensor(device);
    device.frames.add(MotionStartupTestDevice.ack());
    device.busyWhenEmpty = true;
    assertThrows(IOException.class, sensor::getUid);
    assertArrayEquals(new byte[]{0, 0, (byte) 0xff, 0, (byte) 0xff, 0}, device.writes.getLast());
    device.busyWhenEmpty = false;
    device.startup();
    device.tag();
    assertEquals("DEADBEEF", sensor.getUid());
    assertTrue(device.frames.isEmpty());
  }

  @Test
  void malformedTargetPayloadIsAnErrorRatherThanNoTag() {
    Device device = new Device().startup();
    device.exchange(0x4b, 1);
    Pn532Sensor sensor = new Pn532Sensor(device);
    assertThrows(IOException.class, sensor::getUid);
  }

  @Test
  void ultralightCMetadataFitsDocumentedMemoryCapacity() throws Exception {
    Device device = new Device().startup();
    device.ultralightTag();
    for (int index = 0; index < 5; index++) {
      device.ultralightTag();
      device.exchange(0x41, 1);
    }
    device.ultralightTag();
    device.exchange(0x41, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    Pn532Sensor.CardInfo info = new Pn532Sensor(device).getInformation();
    assertEquals(192, info.sizeBytes);
    assertEquals(144, info.userBytes);
    assertEquals(48, info.blockNumber);
    assertTrue(info.userBytes <= info.sizeBytes);
  }

  private static final class Device implements AddressableDevice {
    private final Queue<byte[]> frames = new ArrayDeque<>();
    private final List<byte[]> writes = new ArrayList<>();
    private boolean busyWhenEmpty;

    Device startup() {
      exchange(0x03, 0x32, 1, 6, 7);
      exchange(0x15);
      exchange(0x33);
      return this;
    }

    void exchange(int command, int... data) {
      frames.add(MotionStartupTestDevice.ack());
      frames.add(MotionStartupTestDevice.frame(command, data));
    }

    void tag() {
      exchange(0x4b, 1, 1, 0, 4, 8, 4, 0xde, 0xad, 0xbe, 0xef);
    }

    void ultralightTag() {
      exchange(0x4b, 1, 1, 0, 0x44, 0, 7, 4, 1, 2, 3, 4, 5, 6);
    }

    @Override public void close() { }
    @Override public int getBus() { return 1; }
    @Override public int getDevice() { return 0x24; }
    @Override public int write(int value) { throw new AssertionError("Expected a raw frame write"); }
    @Override public int write(byte[] data, int offset, int length) {
      writes.add(Arrays.copyOfRange(data, offset, offset + length));
      return length;
    }
    @Override public int writeRegister(int register, byte[] data) { throw new AssertionError("Expected a raw frame write"); }
    @Override public int read(byte[] data, int offset, int length) {
      byte[] frame = frames.poll();
      if (frame == null) {
        if (!busyWhenEmpty) return -1;
        data[offset] = 0;
        return 1;
      }
      int count = Math.min(length, frame.length);
      System.arraycopy(frame, 0, data, offset, count);
      return count;
    }
    @Override public int readRegister(int register) { throw new AssertionError("Expected a raw frame read"); }
    @Override public int readRegister(int register, byte[] data, int offset, int length) { throw new AssertionError("Expected a raw frame read"); }
    @Override public int read() { throw new AssertionError("Expected a raw frame read"); }
  }
}
