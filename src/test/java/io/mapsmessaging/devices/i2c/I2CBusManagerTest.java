package io.mapsmessaging.devices.i2c;

import static org.junit.jupiter.api.Assertions.*;

import io.mapsmessaging.devices.i2c.devices.demo.I2cDemoController;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import org.junit.jupiter.api.Test;

class I2CBusManagerTest {
  @Test
  void everyRegisteredPhysicalControllerIsMappedToItsAddresses() throws Exception {
    I2CBusManager manager = new I2CBusManager(null, null, 1);
    Set<Class<?>> registered = new HashSet<>();
    for (I2CDeviceController controller : ServiceLoader.load(I2CDeviceController.class)) {
      if (controller instanceof I2cDemoController) continue;
      registered.add(controller.getClass());
      assertNotNull(manager.knownDevices.get(controller.getName()), controller.getName());
      for (int address : controller.getAddressRange()) {
        assertTrue(address >= 0 && address < 0x80, controller.getName());
        List<I2CDeviceController> candidates = manager.mappedDevices.get(address);
        assertNotNull(candidates, controller.getName());
        assertTrue(candidates.stream().anyMatch(c -> c.getClass().equals(controller.getClass())), controller.getName());
      }
    }
    assertFalse(registered.isEmpty());
    Set<Class<?>> mapped = new HashSet<>();
    for (List<I2CDeviceController> candidates : manager.mappedDevices.values()) {
      for (I2CDeviceController candidate : candidates) {
        mapped.add(candidate.getClass());
      }
    }
    assertEquals(registered, mapped);
    assertNull(manager.configureDevice(0x77, "unknown-controller"));
    assertNull(manager.configureDevices(Map.of()));
    assertTrue(manager.getActive().isEmpty());
    assertNull(manager.get("77"));
  }
}
