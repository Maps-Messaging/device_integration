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
}
