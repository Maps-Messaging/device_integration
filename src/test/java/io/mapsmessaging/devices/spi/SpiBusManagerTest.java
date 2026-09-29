package io.mapsmessaging.devices.spi;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import org.junit.jupiter.api.Test;

class SpiBusManagerTest {
  @Test
  void registeredControllersAreDiscoverableWithoutOpeningSpi() {
    SpiBusManager manager = new SpiBusManager(null);
    List<String> names = ServiceLoader.load(SpiDeviceController.class).stream()
        .map(provider -> provider.get().getName()).toList();
    assertFalse(names.isEmpty());
    assertTrue(names.contains("Mcp3y0x"));
    for (String name : names) {
      assertEquals(name, manager.getDevice(name).getName());
    }
    assertNull(manager.getDevice("unknown-controller"));
    assertNull(manager.mount("unknown-controller", Map.of()));
    assertNull(manager.configureDevice("unknown-controller", Map.of()));
    assertEquals(List.of(), manager.configureDevices(Map.of()));
    assertTrue(manager.getActive().isEmpty());
    assertNull(manager.get("unknown-controller"));
  }
}
