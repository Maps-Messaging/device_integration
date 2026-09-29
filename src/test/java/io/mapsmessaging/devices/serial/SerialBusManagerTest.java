package io.mapsmessaging.devices.serial;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SerialBusManagerTest {
  @Test
  void everyRegisteredControllerIsAvailableWithoutOpeningSerialPort() throws Exception {
    SerialBusManager manager = new SerialBusManager();
    Set<String> names = new HashSet<>();
    for (SerialDeviceController controller : ServiceLoader.load(SerialDeviceController.class)) {
      names.add(controller.getName());
      assertEquals(controller.getClass(), manager.getDevice(controller.getName()).getClass());
    }
    assertFalse(names.isEmpty());
    assertTrue(names.contains("SEN0547"));
    assertNull(manager.getDevice("unknown-controller"));
    assertNull(manager.mount("unknown-controller", null));
    assertTrue(manager.getActive().isEmpty());
  }
}
