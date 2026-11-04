package io.mapsmessaging.devices.i2c.devices.sensors;

import io.mapsmessaging.devices.i2c.devices.sensors.pn532.Pn532Sensor;
import io.mapsmessaging.devices.i2c.devices.sensors.pn532.Pn532Controller;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Pn532StartupRegressionTest {
  @Test
  void pn532AcceptsStatusPrefixedSamAndFirstTagUsingRawTransport() throws Exception {
    MotionStartupTestDevice device = new MotionStartupTestDevice();
    device.frames.add(MotionStartupTestDevice.ack());
    device.frames.add(MotionStartupTestDevice.frame(0x15));
    device.frames.add(MotionStartupTestDevice.ack());
    device.frames.add(MotionStartupTestDevice.frame(0x4b, 1, 1, 0, 4, 8, 4, 0xde, 0xad, 0xbe, 0xef));
    Pn532Sensor sensor = new Pn532Sensor(device) {
      @Override
      public void delay(int milliseconds) {
        // No hardware conversion delay in this fixture.
      }
    };
    assertTrue(sensor.isConnected());
    assertEquals("DEADBEEF", sensor.getUid());
    assertEquals(2, device.rawWrites.size());
    assertArrayEquals(new byte[]{0, 0, (byte) 255, 5, (byte) 251, (byte) 212, 20, 1, 20, 1, 2, 0}, device.rawWrites.get(0));
  }

  @Test
  void pn532RejectsBadChecksumDuringInitialization() {
    MotionStartupTestDevice device = new MotionStartupTestDevice();
    device.frames.add(MotionStartupTestDevice.ack());
    byte[] response = MotionStartupTestDevice.frame(0x15);
    response[8] ^= 1;
    device.frames.add(response);
    assertThrows(UncheckedIOException.class, () -> new Pn532Sensor(device));
  }

  @Test
  void pn532RetriesBusyStatusAndReportsValidNoTagResponse() throws Exception {
    MotionStartupTestDevice device = new MotionStartupTestDevice();
    device.frames.add(new byte[]{0});
    device.frames.add(MotionStartupTestDevice.ack());
    device.frames.add(MotionStartupTestDevice.frame(0x15));
    device.frames.add(MotionStartupTestDevice.ack());
    device.frames.add(MotionStartupTestDevice.frame(0x4b, 0));
    Pn532Sensor sensor = new Pn532Sensor(device) {
      @Override
      public void delay(int milliseconds) {
        // No hardware conversion delay in this fixture.
      }
    };
    assertFalse(sensor.isTagPresent());
    assertEquals("", sensor.getLastUid());
  }

  @Test
  void pn532MountPreservesInitializationIoFailure() {
    MotionStartupTestDevice device = new MotionStartupTestDevice();
    IOException error = assertThrows(IOException.class, () -> new Pn532Controller().mount(device));
    assertTrue(error.getMessage().contains("Failed to read"));
  }
}
