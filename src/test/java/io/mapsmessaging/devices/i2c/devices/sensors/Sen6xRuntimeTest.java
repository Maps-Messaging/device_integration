package io.mapsmessaging.devices.i2c.devices.sensors;

import static io.mapsmessaging.devices.i2c.devices.sensors.Sen6xProtocolTest.frame;
import static io.mapsmessaging.devices.i2c.devices.sensors.Sen6xProtocolTest.helper;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.mapsmessaging.devices.i2c.devices.sensors.sen6x.Sen65Sensor;
import io.mapsmessaging.devices.i2c.devices.sensors.sen6x.Sen66Sensor;
import io.mapsmessaging.devices.i2c.devices.sensors.sen6x.Sen68Sensor;
import io.mapsmessaging.devices.i2c.devices.sensors.sen6x.Sen6xSensor;
import io.mapsmessaging.devices.i2c.devices.sensors.sen6x.Sen6xCommandHelper;
import io.mapsmessaging.devices.i2c.devices.sensors.sen6x.commands.Sen65MeasurementManager;
import io.mapsmessaging.devices.i2c.devices.sensors.sen6x.commands.Sen6xMeasurementManager;
import io.mapsmessaging.devices.i2c.devices.sensors.sen6x.data.MeasurementBlock;
import io.mapsmessaging.devices.sensorreadings.SensorReading;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class Sen6xRuntimeTest {

  @Test
  void warmupDoesNotReportHazardousAirQuality() {
    Sen66Sensor sensor = new Sen66Sensor(runtimeBus("SEN66", frame(0), frame(0)));
    assertTrue(reading(sensor, "airQualityIndex").getValue().hasError());
    assertEquals("Unknown", reading(sensor, "airQualityValue").getValue().getResult());
  }

  @Test
  void cacheExpiresWhenNoNewMeasurementsArrive() throws Exception {
    ScriptedI2CDevice bus = new ScriptedI2CDevice(0x6b, frame(1), frame(100, 250, 400, 1000, 5000, 4000, 1000, 10), frame(0));
    Sen65MeasurementManager manager = new Sen65MeasurementManager(helper(bus));
    assertEquals(25.0f, manager.getMeasurementBlock().getPm2_5());
    expire(manager);
    assertThrows(IOException.class, manager::getMeasurementBlock);
  }

  @Test
  void sen65RecoversThroughParticulateReadingWhenDataReadyStalls() throws Exception {
    assertRecovery(new Sen65Sensor(runtimeBus("SEN65", frame(0))));
  }

  @Test
  void sen68RecoversThroughParticulateReadingWhenDataReadyStalls() throws Exception {
    assertRecovery(new Sen68Sensor(runtimeBus("SEN68", frame(0))));
  }

  @Test
  void sen66RecoversWhenDataReadyReadFails() throws Exception {
    assertRecovery(new Sen66Sensor(runtimeBus("SEN66", new byte[] {0, 1, 0})));
  }

  @Test
  void sen66RecoversWhenMeasurementCrcFails() throws Exception {
    assertRecovery(new Sen66Sensor(runtimeBus("SEN66", frame(1), new byte[27])));
  }

  @Test
  void failedRecoveryCanRetryWithoutRestartingIntentionallyStoppedSensors() throws Exception {
    ScriptedI2CDevice delegate = runtimeBus("SEN65", frame(0), frame(0));
    boolean[] failReset = {false};
    ScriptedI2CDevice bus = new ScriptedI2CDevice(0x6b) {
      @Override
      public int read(byte[] data, int offset, int length) {
        return delegate.read(data, offset, length);
      }

      @Override
      public int write(byte[] data, int offset, int length) {
        if (failReset[0] && data[offset] == (byte) 0xd3) {
          return -1;
        }
        return super.write(data, offset, length);
      }
    };
    Sen65Sensor sensor = new Sen65Sensor(bus);
    expire(sensor.getMeasurementManager());
    failReset[0] = true;
    IOException failure = assertThrows(IOException.class, () -> sensor.getMeasurementManager().getMeasurementBlock());
    assertEquals(1, failure.getSuppressed().length);
    assertFalse(sensor.isMeasuring());

    failReset[0] = false;
    expire(sensor.getMeasurementManager());
    sensor.getMeasurementManager().getMeasurementBlock();
    assertTrue(sensor.isMeasuring());
    sensor.powerOff();
    expire(sensor.getMeasurementManager());
    bus.writes().clear();
    assertThrows(IOException.class, () -> sensor.getMeasurementManager().getMeasurementBlock());
    assertTrue(bus.writes().isEmpty());
  }

  @Test
  void healthyMeasurementAfterLongPollingGapDoesNotResetSensor() throws Exception {
    Sen65Sensor sensor = new Sen65Sensor(runtimeBus("SEN65", frame(1), frame(100, 250, 400, 1000, 5000, 4000, 1000, 10)));
    expire(sensor.getMeasurementManager());
    ScriptedI2CDevice bus = (ScriptedI2CDevice) sensor.getDevice();
    bus.writes().clear();
    assertEquals(25.0f, sensor.getMeasurementManager().getMeasurementBlock().getPm2_5());
    assertFalse(commands(bus).contains(0xd304));
  }

  @Test
  void stopWaitsUntilInProgressMeasurementHasFinished() throws Exception {
    CountDownLatch decoding = new CountDownLatch(1);
    CountDownLatch finishDecoding = new CountDownLatch(1);
    CountDownLatch stopping = new CountDownLatch(1);
    CountDownLatch stopSent = new CountDownLatch(1);
    ScriptedI2CDevice delegate = runtimeBus("SEN65", frame(1), frame(100, 250, 400, 1000, 5000, 4000, 1000, 10));
    ScriptedI2CDevice bus = new ScriptedI2CDevice(0x6b) {
      @Override
      public int read(byte[] data, int offset, int length) {
        return delegate.read(data, offset, length);
      }

      @Override
      public int write(byte[] data, int offset, int length) {
        if (stopping.getCount() == 0 && data[offset] == 0x01 && data[offset + 1] == 0x04) {
          stopSent.countDown();
        }
        return super.write(data, offset, length);
      }
    };
    Sen65Sensor sensor = new Sen65Sensor(bus) {
      @Override
      protected Sen6xMeasurementManager contructMeasurementManager(Sen6xCommandHelper helper) {
        return new Sen65MeasurementManager(helper) {
          @Override
          protected MeasurementBlock processResponse(byte[] data) throws IOException {
            decoding.countDown();
            try {
              if (!finishDecoding.await(5, TimeUnit.SECONDS)) {
                throw new IOException("Test did not release measurement decoding");
              }
            } catch (InterruptedException exception) {
              Thread.currentThread().interrupt();
              throw new IOException("Interrupted test measurement", exception);
            }
            return super.processResponse(data);
          }
        };
      }
    };
    ExecutorService workers = Executors.newFixedThreadPool(2);
    try {
      Future<MeasurementBlock> measurement = workers.submit(() -> sensor.getMeasurementManager().getMeasurementBlock());
      assertTrue(decoding.await(5, TimeUnit.SECONDS));
      Future<?> stop = workers.submit(() -> {
        stopping.countDown();
        sensor.powerOff();
      });
      assertTrue(stopping.await(5, TimeUnit.SECONDS));
      assertFalse(stopSent.await(100, TimeUnit.MILLISECONDS), "Stop command overlapped an active measurement read");
      finishDecoding.countDown();
      assertEquals(25.0f, measurement.get(5, TimeUnit.SECONDS).getPm2_5());
      stop.get(5, TimeUnit.SECONDS);
      assertFalse(sensor.isMeasuring());
      assertEquals(0, stopSent.getCount());
    } finally {
      finishDecoding.countDown();
      workers.shutdownNow();
      assertTrue(workers.awaitTermination(5, TimeUnit.SECONDS));
    }
  }

  @Test
  void resetResumesPreviouslyRunningMeasurement() throws Exception {
    Sen66Sensor sensor = new Sen66Sensor(runtimeBus("SEN66"));
    ScriptedI2CDevice bus = (ScriptedI2CDevice) sensor.getDevice();
    bus.writes().clear();
    sensor.reset();
    assertTrue(sensor.isMeasuring());
    assertEquals(List.of(0x0104, 0xd304, 0x0021), commands(bus));
  }

  @Test
  void resetKeepsIntentionallyStoppedSensorStopped() throws Exception {
    Sen66Sensor sensor = new Sen66Sensor(runtimeBus("SEN66"));
    sensor.powerOff();
    sensor.softReset();
    assertFalse(sensor.isMeasuring());
  }

  @Test
  void stoppedSensorRejectsMeasurementReadsWithoutBusIo() {
    Sen66Sensor sensor = new Sen66Sensor(runtimeBus("SEN66", frame(0)));
    sensor.powerOff();
    ScriptedI2CDevice bus = (ScriptedI2CDevice) sensor.getDevice();
    bus.writes().clear();
    assertThrows(IOException.class, () -> sensor.getMeasurementManager().getMeasurementBlock());
    assertTrue(bus.writes().isEmpty());
  }

  @Test
  void closeStopsMeasurementBeforeClosingBusAndIsIdempotent() {
    ScriptedI2CDevice bus = runtimeBus("SEN66");
    Sen66Sensor sensor = new Sen66Sensor(bus);
    bus.writes().clear();
    sensor.close();
    sensor.close();
    assertFalse(sensor.isMeasuring());
    assertEquals(List.of(0x0104), commands(bus));
  }

  @Test
  void failedStopStillClosesBusAndReportsFailure() {
    ScriptedI2CDevice delegate = runtimeBus("SEN66");
    boolean[] failStop = {false};
    boolean[] closed = {false};
    ScriptedI2CDevice bus = new ScriptedI2CDevice(0x6b) {
      @Override
      public int read(byte[] data, int offset, int length) {
        return delegate.read(data, offset, length);
      }

      @Override
      public int write(byte[] data, int offset, int length) {
        return failStop[0] ? -1 : super.write(data, offset, length);
      }

      @Override
      public void close() {
        closed[0] = true;
      }
    };
    Sen66Sensor sensor = new Sen66Sensor(bus);
    failStop[0] = true;
    assertThrows(UncheckedIOException.class, sensor::close);
    assertTrue(closed[0]);
    assertFalse(sensor.isMeasuring());
    assertThrows(IOException.class, sensor::powerOn);
  }

  private static void assertRecovery(Sen6xSensor sensor) throws Exception {
    expire(sensor.getMeasurementManager());
    ScriptedI2CDevice bus = (ScriptedI2CDevice) sensor.getDevice();
    bus.writes().clear();
    assertTrue(reading(sensor, "pm_2_5").getValue().hasError());
    assertTrue(sensor.isMeasuring());
    List<Integer> lifecycleCommands = commands(bus).stream().filter(command -> command == 0x0104 || command == 0xd304 || command == 0x0021).toList();
    assertEquals(List.of(0x0104, 0xd304, 0x0021), lifecycleCommands);
    assertFalse(sensor.getMeasurementManager().hasLocked());
  }

  private static SensorReading<?> reading(Sen6xSensor sensor, String name) {
    return sensor.getReadings().stream().filter(reading -> reading.getName().equals(name)).findFirst().orElseThrow();
  }

  private static void expire(Sen6xMeasurementManager manager) throws Exception {
    Field lastReadTime = Sen6xMeasurementManager.class.getDeclaredField("lastReadTime");
    lastReadTime.setAccessible(true);
    lastReadTime.setLong(manager, System.currentTimeMillis() - 16000);
  }

  private static List<Integer> commands(ScriptedI2CDevice bus) {
    return bus.writes().stream().map(bytes -> ((bytes[0] & 0xff) << 8) | (bytes[1] & 0xff)).toList();
  }

  private static ScriptedI2CDevice runtimeBus(String model, byte[]... responses) {
    ScriptedI2CDevice identity = Sen6xCapabilitiesTest.bus(model);
    ScriptedI2CDevice measurements = new ScriptedI2CDevice(0x6b, responses);
    return new ScriptedI2CDevice(0x6b) {
      @Override
      public int read(byte[] data, int offset, int length) {
        if (length == 48) {
          return identity.read(data, offset, length);
        }
        return measurements.read(data, offset, length);
      }
    };
  }
}
