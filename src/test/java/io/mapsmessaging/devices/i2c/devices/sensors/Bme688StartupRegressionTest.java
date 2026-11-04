package io.mapsmessaging.devices.i2c.devices.sensors;

import io.mapsmessaging.devices.i2c.devices.sensors.bme688.BME688Sensor;
import io.mapsmessaging.devices.impl.AddressableDevice;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Bme688StartupRegressionTest {
  @Test
  void bme688DoesNotPublishInitialZerosOrRestartAfterPowerOff() throws Exception {
    SensorRegisterDevice device = new SensorRegisterDevice();
    BME688Sensor sensor = new FastBme688(device);
    assertThrows(IOException.class, sensor::getTemperature);
    sensor.powerOff();
    assertEquals(0, device.value(0x74) & 3);
    assertThrows(IOException.class, sensor::getTemperature);
    assertEquals(0, device.value(0x74) & 3);
  }

  @Test
  void bme688RequiresNewDataAfterInitialDeadline() throws Exception {
    BME688Sensor sensor = new FastBme688(new SensorRegisterDevice());
    Thread.sleep(1050);
    assertThrows(IOException.class, sensor::getTemperature);
  }

  @Test
  void dewPointPreservesUnavailableMeasurementFailure() throws Exception {
    BME688Sensor sensor = new FastBme688(new SensorRegisterDevice());
    UncheckedIOException failure = assertThrows(UncheckedIOException.class, sensor::getDewPoint);
    assertTrue(failure.getCause().getMessage().contains("not ready"));
    sensor.powerOff();
    assertThrows(UncheckedIOException.class, sensor::getDewPoint);
  }

  private static class FastBme688 extends BME688Sensor {
    FastBme688(AddressableDevice device) throws IOException {
      super(device);
    }
    @Override
    public void delay(int milliseconds) {
      // Hardware waits are controlled by each test.
    }
  }
}
