package io.mapsmessaging.devices.i2c.devices.output.lcd.lcd1602;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.mapsmessaging.devices.i2c.RegisterBackedI2cDevice;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import io.mapsmessaging.devices.i2c.I2CDeviceScheduler;
import io.mapsmessaging.devices.i2c.devices.output.lcd.lcd1602.task.Clock;
import org.junit.jupiter.api.Test;

class Lcd1602DatasheetTest {

  @Test
  void documentedJsonCommandCanWriteReadAndClearWithoutClassDiscriminator() throws Exception {
    Lcd1602Controller controller = new Lcd1602Controller(new RegisterBackedI2cDevice(0x3e));
    byte[] write = "{\"action\":\"WRITE\",\"address\":0,\"data\":[65,66]}".getBytes(StandardCharsets.UTF_8);
    byte[] response = controller.updateDeviceConfiguration(write);
    assertTrue(new String(response, StandardCharsets.UTF_8).contains("success"));
    Lcd1602Device display = (Lcd1602Device) controller.getDevice();
    assertArrayEquals(new byte[] {65, 66}, display.readBlock(0, 2));

    controller.updateDeviceConfiguration("{\"deviceWrite\":{\"action\":\"CLEAR\"}}".getBytes(StandardCharsets.UTF_8));
    assertArrayEquals(new byte[] {0, 0}, display.readBlock(0, 2));
    assertArrayEquals(new byte[] {0, 0}, display.readBlock(0, 2));
  }

  @Test
  void runningClockDoesNotHoldBusLockAcrossDisplayDelays() throws Exception {
    Lcd1602Controller controller = new Lcd1602Controller(new RegisterBackedI2cDevice(0x3e));
    Clock task = new Clock(controller);
    Thread thread = new Thread(task, "lcd-clock-lock-test");
    thread.setDaemon(true);
    thread.start();
    try {
      Thread.sleep(50);
      CompletableFuture<Void> contender = CompletableFuture.runAsync(() -> {
        synchronized (I2CDeviceScheduler.getI2cBusLock()) {
          // Another I2C device must be able to proceed while the clock runs.
        }
      });
      contender.get(1, TimeUnit.SECONDS);
    } finally {
      task.stop();
      thread.join(2000);
    }
  }

  @Test
  void boundedReadRejectsNegativeAddresses() throws Exception {
    Lcd1602Controller controller = new Lcd1602Controller(new RegisterBackedI2cDevice(0x3e));
    Lcd1602Device display = (Lcd1602Device) controller.getDevice();
    assertThrows(IllegalArgumentException.class, () -> display.readBlock(-1, 2));
  }
}
