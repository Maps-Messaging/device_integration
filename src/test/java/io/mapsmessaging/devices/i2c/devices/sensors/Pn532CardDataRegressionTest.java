package io.mapsmessaging.devices.i2c.devices.sensors;

import io.mapsmessaging.devices.i2c.devices.sensors.pn532.Pn532Sensor;
import java.io.IOException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Pn532CardDataRegressionTest {
  @Test
  void ntagReadReturnsSixteenBytesFollowingStatus() throws Exception {
    MotionStartupTestDevice device = deviceWithStartup();
    tag(device);
    exchange(device, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);
    assertArrayEquals(sequence(), sensor(device).readNTAG(4));
  }

  @Test
  void mifareReadAcceptsStatusOnlyAuthenticationThenReadData() throws Exception {
    MotionStartupTestDevice device = deviceWithStartup();
    tag(device);
    tag(device);
    exchange(device, 0);
    exchange(device, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);
    assertArrayEquals(sequence(), sensor(device).readMifareBlock(4));
  }

  @Test
  void mifareReadRejectsFailedAuthentication() throws Exception {
    MotionStartupTestDevice device = deviceWithStartup();
    tag(device);
    tag(device);
    exchange(device, 0x14);
    assertThrows(IOException.class, () -> sensor(device).readMifareBlock(4));
  }

  @Test
  void ultralightProbeAcceptsSuccessfulReadAndRejectsErrorStatus() throws Exception {
    MotionStartupTestDevice device = deviceWithStartup();
    tag(device);
    exchange(device, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);
    tag(device);
    exchange(device, 1);
    Pn532Sensor sensor = sensor(device);
    assertTrue(sensor.getUltraversion(230));
    assertFalse(sensor.getUltraversion(230));
  }

  @Test
  void ntagWriteRejectsDeviceErrorStatus() throws Exception {
    MotionStartupTestDevice device = deviceWithStartup();
    tag(device);
    exchange(device, 1);
    assertThrows(IOException.class, () -> sensor(device).writeNTAG(4, new byte[4]));
  }

  @Test
  void mifareWriteRejectsDeviceErrorAfterSuccessfulAuthentication() throws Exception {
    MotionStartupTestDevice device = deviceWithStartup();
    tag(device);
    tag(device);
    exchange(device, 0);
    exchange(device, 1);
    IOException error = assertThrows(IOException.class, () -> sensor(device).writeMifareBlock(4, new byte[16]));
    assertTrue(error.getMessage().contains("write"));
  }

  @Test
  void writesAcceptStatusOnlySuccessResponses() throws Exception {
    MotionStartupTestDevice device = deviceWithStartup();
    tag(device);
    exchange(device, 0);
    tag(device);
    tag(device);
    exchange(device, 0);
    exchange(device, 0);
    Pn532Sensor sensor = sensor(device);
    sensor.writeNTAG(4, new byte[4]);
    sensor.writeMifareBlock(4, new byte[16]);
  }

  private static MotionStartupTestDevice deviceWithStartup() {
    MotionStartupTestDevice device = new MotionStartupTestDevice();
    device.frames.add(MotionStartupTestDevice.ack());
    device.frames.add(MotionStartupTestDevice.frame(0x15));
    return device;
  }

  private static Pn532Sensor sensor(MotionStartupTestDevice device) {
    return new Pn532Sensor(device) {
      @Override
      public void delay(int milliseconds) {
        // Scripted complete responses require no hardware delay.
      }
    };
  }

  private static void tag(MotionStartupTestDevice device) {
    device.frames.add(MotionStartupTestDevice.ack());
    device.frames.add(MotionStartupTestDevice.frame(0x4b, 1, 1, 0, 4, 8, 4, 0xde, 0xad, 0xbe, 0xef));
  }

  private static void exchange(MotionStartupTestDevice device, int... payload) {
    device.frames.add(MotionStartupTestDevice.ack());
    device.frames.add(MotionStartupTestDevice.frame(0x41, payload));
  }

  private static byte[] sequence() {
    return new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
  }
}
