package io.mapsmessaging.devices.buses;

import static org.junit.jupiter.api.Assertions.*;
import io.mapsmessaging.devices.onewire.devices.ds18b20.DS18B20Device;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BusDeviceOneWireTest {
  @TempDir Path directory;
  @Test void kernelFileSamplesScaleMillidegreesAndTrackPositiveExtremaAndDelta() throws Exception {
    Path sample = directory.resolve("w1_slave");
    Files.writeString(sample, "a0 01 4b 46 7f ff 10 10 80 : crc=80 YES\na0 01 4b 46 7f ff 10 10 80 t=26000\n");
    DS18B20Device sensor = new DS18B20Device(sample.toFile());
    sensor.update();
    assertEquals(26f, sensor.getCurrent());
    assertEquals(26f, sensor.getMin());
    assertEquals(26f, sensor.getMax());
    Files.writeString(sample, "90 01 4b 46 7f ff 10 10 ff : crc=ff YES\n90 01 4b 46 7f ff 10 10 ff t=25250\n");
    sensor.update();
    assertEquals(25.25f, sensor.getCurrent());
    assertEquals(25.25f, sensor.getMin());
    assertEquals(26f, sensor.getMax());
    assertEquals(-0.75f, sensor.getDif());
  }
  @Test void invalidCrcDoesNotReplaceLastValidTemperature() throws Exception {
    Path sample = directory.resolve("w1_slave");
    Files.writeString(sample, "50 05 4b 46 7f ff 0c 10 1c : crc=1c YES\\n50 05 4b 46 7f ff 0c 10 1c t=85000\\n".replace("\\n", "\n"));
    DS18B20Device sensor = new DS18B20Device(sample.toFile());
    sensor.update();
    Files.writeString(sample, "50 05 4b 46 7f ff 0c 10 1c : crc=1c NO\\n50 05 4b 46 7f ff 0c 10 1c t=5000\\n".replace("\\n", "\n"));
    assertThrows(java.io.UncheckedIOException.class, sensor::update);
    assertEquals(85f, sensor.getCurrent());
  }

  @Test void negativeTemperaturesAndStableReadingsMaintainCorrectExtremaAndDelta() throws Exception {
    Path sample = directory.resolve("w1_slave");
    DS18B20Device sensor = new DS18B20Device(sample.toFile());
    Files.writeString(sample, "f0 ff 4b 46 7f ff 10 10 c5 : crc=c5 YES\\nf0 ff 4b 46 7f ff 10 10 c5 t=-1000\\n".replace("\\n", "\n"));
    sensor.update();
    assertEquals(-1f, sensor.getMin());
    assertEquals(-1f, sensor.getMax());
    Files.writeString(sample, "f0 ff 4b 46 7f ff 10 10 c5 : crc=c5 YES\\nf0 ff 4b 46 7f ff 10 10 c5 t=-1000\\n".replace("\\n", "\n"));
    sensor.update();
    assertEquals(0f, sensor.getDif());
  }

}
