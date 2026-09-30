package io.mapsmessaging.devices.i2c.devices.sensors;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.mapsmessaging.devices.i2c.devices.sensors.sen6x.Sen6xCommandHelper;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class Sen6xDelayTest {

  @Test
  @Timeout(3)
  void deviceNotificationDoesNotEndCommandDelay() throws Exception {
    ScriptedI2CDevice device = new ScriptedI2CDevice(0x6b);
    Thread worker = new Thread(() -> new Sen6xCommandHelper(device).delay(1000));
    worker.start();
    try {
      long deadline = System.nanoTime() + Duration.ofSeconds(1).toNanos();
      while (worker.getState() != Thread.State.TIMED_WAITING && System.nanoTime() < deadline) {
        Thread.yield();
      }
      assertTrue(worker.getState() == Thread.State.TIMED_WAITING, "Command delay did not start");
      synchronized (device) {
        device.notifyAll();
      }
      worker.join(100);
      assertTrue(worker.isAlive(), "Device notification ended the command delay early");
    } finally {
      worker.interrupt();
      worker.join();
    }
  }
}
