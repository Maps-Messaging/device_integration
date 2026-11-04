package io.mapsmessaging.devices.i2c.devices.sensors;

import io.mapsmessaging.devices.i2c.devices.sensors.sht31.Sht31Sensor;
import io.mapsmessaging.devices.i2c.devices.sensors.sht31.commands.SoftResetCommand;
import java.util.Arrays;
import java.io.UncheckedIOException;
import io.mapsmessaging.devices.i2c.devices.sensors.sht31.commands.Command;
import io.mapsmessaging.devices.sensorreadings.ComputationResult;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static io.mapsmessaging.devices.i2c.devices.sensors.Sen6xProtocolTest.frame;

class Sht31StartupRegressionTest {
  @Test
  void shtResetWaitsAndInitialConversionPrecedesFetch() throws Exception {
    assertTrue(new SoftResetCommand().getDelayTime() >= 2);
    ScriptedI2CDevice bus = new ScriptedI2CDevice(0x44, frame(32768, 32768));
    Sht31Sensor sensor = new Sht31Sensor(bus) {
      @Override
      public void delay(int millis) {
        if (millis >= 10) {
          assertFalse(bus.writes().stream().anyMatch(bytes -> Arrays.equals(bytes, new byte[] {(byte) 0xe0, 0})));
        }
      }
    };
    assertEquals(42.5013f, sensor.getTemperature(), 0.001f);
  }

  @Test
  void shtStopsExistingPeriodicModeBeforeReset() throws Exception {
    ScriptedI2CDevice bus = new ScriptedI2CDevice(0x44);
    Sht31Sensor sensor = new Sht31Sensor(bus) {
      @Override
      public void delay(int millis) {
      }
    };
    assertArrayEquals(new byte[] {0x30, (byte) 0x93}, bus.writes().get(0));
  }

  @Test
  void shortAndCorruptInitialFramesBecomeReadingErrorsAndRetryImmediately() throws Exception {
    byte[] corrupt = frame(32768, 32768);
    corrupt[5] ^= 1;
    for (byte[] invalid : new byte[][] {Arrays.copyOf(frame(32768, 32768), 3), corrupt}) {
      ScriptedI2CDevice bus = new ScriptedI2CDevice(0x44, invalid, frame(32768, 32768));
      Sht31Sensor sensor = new Sht31Sensor(bus) {
        @Override
        public void delay(int millis) {
        }
      };
      SensorReading<?> temperature = sensor.getReadings().stream().filter(reading -> reading.getName().equals("Temperature")).findFirst().orElseThrow();
      ComputationResult<?> initial = temperature.getValue();
      assertTrue(initial.hasError());
      assertInstanceOf(java.io.IOException.class, initial.getError());
      ComputationResult<?> retry = temperature.getValue();
      assertFalse(retry.hasError());
      assertEquals(42.5f, ((Number) retry.getResult()).floatValue(), 0.1f);
    }
  }

  @Test
  void partialCommandWritesAreRejected() {
    ScriptedI2CDevice bus = new ScriptedI2CDevice(0x44) {
      @Override
      public int write(byte[] bytes, int offset, int length) {
        return length - 1;
      }
    };
    assertThrows(UncheckedIOException.class, () -> new SoftResetCommand().sendCommand(null, bus));
  }

  @Test
  void interruptedCommandDoesNotWriteOrRead() throws Exception {
    ScriptedI2CDevice bus = new ScriptedI2CDevice(0x44, frame(32768, 32768));
    new Sht31Sensor(bus) {
      @Override
      public void delay(int millis) {
      }
    };
    Sht31Sensor interruptedSensor = new Sht31Sensor(new ScriptedI2CDevice(0x44)) {
      @Override
      public void delay(int millis) {
      }
    };
    Thread.currentThread().interrupt();
    try {
      assertThrows(UncheckedIOException.class, () -> new Command(0xe000, 1, 6) { }.sendCommand(interruptedSensor, bus));
      assertTrue(Thread.currentThread().isInterrupted());
      assertEquals(3, bus.writes().size());
    } finally {
      Thread.interrupted();
    }
  }
  @Test
  void interruptedDelayPreservesFlagAndStopsBeforeResponseRead() throws Exception {
    boolean[] interruptDelay = {false};
    int[] reads = {0};
    ScriptedI2CDevice bus = new ScriptedI2CDevice(0x44, frame(32768, 32768)) {
      @Override
      public int read(byte[] buffer, int offset, int length) {
        reads[0]++;
        return super.read(buffer, offset, length);
      }
    };
    Sht31Sensor sensor = new Sht31Sensor(bus) {
      @Override
      public void delay(int millis) {
        if (interruptDelay[0]) {
          Thread.currentThread().interrupt();
        }
      }
    };
    interruptDelay[0] = true;
    try {
      UncheckedIOException failure = assertThrows(UncheckedIOException.class,
          () -> new Command(0xe000, 1, 6) { }.sendCommand(sensor, bus));
      assertInstanceOf(java.io.InterruptedIOException.class, failure.getCause());
      assertTrue(Thread.currentThread().isInterrupted());
      assertEquals(0, reads[0]);
    } finally {
      Thread.interrupted();
    }
  }

}
