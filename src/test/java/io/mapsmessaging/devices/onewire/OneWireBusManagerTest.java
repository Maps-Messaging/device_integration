package io.mapsmessaging.devices.onewire;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ServiceLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class OneWireBusManagerTest {
  @TempDir Path root;

  @Test
  void discoversRegisteredDevicesFromSimulatedSysfs() throws Exception {
    assertTrue(ServiceLoader.load(OneWireDeviceController.class).iterator().hasNext());
    Path sensor = Files.createDirectory(root.resolve("28-000001"));
    Files.writeString(sensor.resolve("w1_slave"), "aa bb cc : crc=00 YES\naa bb cc t=21500\n");
    Files.createDirectory(root.resolve("28-incomplete"));
    Files.createDirectory(root.resolve("unregistered"));

    OneWireBusManager manager = new OneWireBusManager(root.toFile());
    assertEquals(1, manager.getActive().size());
    assertEquals("DS18B20", manager.get("28-000001").getName());
    assertNull(manager.get("28-incomplete"));
    assertNull(manager.get("unregistered"));
    assertTrue(new String(manager.get("28-000001").getDeviceState()).contains("21.5"));
    manager.scan();
    assertEquals(1, manager.getActive().size());
  }
}
