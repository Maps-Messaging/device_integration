package io.mapsmessaging.devices.spi;

import com.pi4j.context.Context;
import io.mapsmessaging.devices.spi.devices.mcp3y0x.Mcp3y0xController;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import org.junit.jupiter.api.Test;

class SpiBusManagerTest {
  static class SimulatedAdc extends Mcp3y0xController {
    @Override
    public SpiDeviceController mount(Context context, Map<String, String> config) {
      return this;
    }

    @Override
    public byte[] getDeviceState() {
      return "{\"current\":[]}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }
  }

  @Test
  void mountsAndReadsSimulatedSpiController() throws Exception {
    SpiBusManager manager = new SpiBusManager(null, List.of(new SimulatedAdc()));
    SpiDeviceController mounted = manager.configureDevice("Mcp3y0x", Map.of());
    assertNotNull(mounted);
    assertSame(mounted, manager.getDevice("Mcp3y0x"));
    assertEquals("{\"current\":[]}", new String(manager.get("Mcp3y0x").getDeviceState()));
    assertEquals(1, manager.getActive().size());
  }

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
