package io.mapsmessaging.devices.buses;

import io.mapsmessaging.devices.onewire.devices.ds18b20.DS18B20Controller;
import io.mapsmessaging.devices.onewire.devices.ds18b20.DS18B20Device;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OneWireStartupRegressionTest {
  @TempDir
  Path directory;

  @Test
  void absentOneWireFileCannotPublishInitialZero() {
    DS18B20Controller controller = new DS18B20Controller(directory.resolve("missing").toFile());
    assertThrows(UncheckedIOException.class, controller::getDeviceState);
  }

  @Test
  void negativeAndZeroFirstSamplesInitializeExtremaAndUnchangedSampleClearsDelta() throws Exception {
    Path sample = directory.resolve("w1_slave");
    DS18B20Device sensor = new DS18B20Device(sample.toFile());
    assertThrows(UncheckedIOException.class, sensor::getCurrent);
    writeSample(sample, -5000, true);
    sensor.update();
    assertEquals(-5f, sensor.getMax());
    assertEquals(-5f, sensor.getMin());
    writeSample(sample, 0, true);
    sensor.update();
    assertEquals(5f, sensor.getDif());
    sensor.update();
    assertEquals(0f, sensor.getDif());
    DS18B20Device zero = new DS18B20Device(sample.toFile());
    zero.update();
    assertEquals(0f, zero.getMin());
    assertEquals(0f, zero.getMax());
  }

  @Test
  void crcFailedAndMalformedOneWireSamplesAreRejected() throws Exception {
    Path sample = directory.resolve("w1_slave");
    DS18B20Device sensor = new DS18B20Device(sample.toFile());
    writeSample(sample, 85000, false);
    assertThrows(UncheckedIOException.class, sensor::update);
    Files.writeString(sample, "bytes : crc=00 YES\nbytes t=garbage\n");
    UncheckedIOException failure = assertThrows(UncheckedIOException.class, sensor::update);
    assertEquals(NumberFormatException.class, failure.getCause().getCause().getClass());
  }

  private void writeSample(Path sample, int temperature, boolean valid) throws IOException {
    Files.writeString(sample, "bytes : crc=00 " + (valid ? "YES" : "NO") + "\nbytes t=" + temperature + "\n");
  }

}
