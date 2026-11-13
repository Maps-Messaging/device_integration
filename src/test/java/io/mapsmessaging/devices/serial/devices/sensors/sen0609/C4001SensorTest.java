package io.mapsmessaging.devices.serial.devices.sensors.sen0609;

import io.mapsmessaging.devices.serial.devices.sensors.SerialDevice;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Queue;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class C4001SensorTest {
  @Test void parsePresenceAndSpeedReports() {
    Instant timestamp = Instant.EPOCH;
    C4001Observation presence = C4001Sensor.parse("noise$DFHPD,1*", timestamp);
    assertNotNull(presence);
    assertTrue(presence.present());
    assertNull(presence.distanceMetres());
    C4001Observation speed = C4001Sensor.parse("$DFDMD,1,0,3.25,0.75,55,0*", timestamp);
    assertEquals(C4001Observation.Mode.SPEED, speed.mode());
    assertEquals(Double.valueOf(3.25), speed.distanceMetres());
    assertEquals(Double.valueOf(0.75), speed.velocityMetresPerSecond());
    assertEquals(Integer.valueOf(55), speed.energy());
    assertNull(C4001Sensor.parse("$DFDMD,1,0,corrupt,1,4*", timestamp));
  }

  @Test void thresholdsAccumulateAndPresenceTransitionsAlwaysTrigger() {
    C4001Differ differ = new C4001Differ();
    C4001Observation base = observation(true, 3.0, 0.1);
    C4001Observation noise = observation(true, 3.05, 0.12);
    assertTrue(differ.diff(base, noise).changedFields().isEmpty());
    C4001Observation moved = observation(true, 3.30, 0.12);
    assertTrue(differ.diff(base, moved).changedFields().contains("distanceMetres"));
    assertTrue(differ.diff(moved, observation(false, null, null)).changedFields().contains("presence"));
    assertThrows(IllegalArgumentException.class, () -> differ.setDistanceThresholdMetres(Double.NaN));
  }

  @Test void independentQueuesAndStreamingFrames() throws Exception {
    FakeSerial serial = new FakeSerial();
    C4001Sensor radar = new C4001Sensor(serial, 2, 2);
    try {
      serial.inject("$DFHPD,0*\r\n$DFHPD,1*\r\n$DFHPD,1*\r\n$DFHPD,0*\r\n");
      long deadline = System.nanoTime() + 1_000_000_000L;
      while ((radar.queueDepth() < 2 || radar.changes().queueDepth() < 2) && System.nanoTime() < deadline) {
        Thread.sleep(1);
      }
      assertTrue(radar.hasOverrun());
      assertFalse(radar.hasOverrun());
      assertEquals(2, radar.queueDepth());
      assertTrue(radar.changes().hasData());
      assertNotNull(radar.getData());
      assertTrue(radar.changes().hasData());
    } finally { radar.close(); }
  }

  private static C4001Observation observation(boolean present, Double distance, Double speed) {
    return new C4001Observation(Instant.EPOCH, C4001Observation.Mode.SPEED,
        present, present ? 1 : 0, distance, speed, present ? 20 : null);
  }

  private static class FakeSerial implements SerialDevice {
    private final Queue<Byte> inbound = new ArrayDeque<>();
    private volatile boolean open;
    synchronized void inject(String text) {
      for (byte b : text.getBytes(StandardCharsets.US_ASCII)) inbound.add(b);
    }
    @Override public boolean isOpen() { return open; }
    @Override public boolean openPort() { open = true; return true; }
    @Override public String getSystemPortName() { return "fixture"; }
    @Override public void closePort() { open = false; }
    @Override public int writeBytes(byte[] value, int length) { return length; }
    @Override public synchronized int readBytes(byte[] buffer, int remaining) {
      int count = 0;
      while (count < remaining && !inbound.isEmpty()) buffer[count++] = inbound.remove();
      return count;
    }
  }
}
