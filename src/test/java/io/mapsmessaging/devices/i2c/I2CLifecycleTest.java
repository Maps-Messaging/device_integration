package io.mapsmessaging.devices.i2c;

import static org.junit.jupiter.api.Assertions.*;

import com.pi4j.io.i2c.I2C;
import com.pi4j.Pi4J;
import com.pi4j.io.i2c.I2CProvider;
import io.mapsmessaging.devices.i2c.devices.sensors.bh1750.Bh1750Controller;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class I2CLifecycleTest {
  @Test
  void closeRemovesHandleAndRemountCreatesFreshDevice() throws Exception {
    AtomicInteger firstCloses = new AtomicInteger();
    AtomicInteger freshCloses = new AtomicInteger();
    AtomicInteger creates = new AtomicInteger();
    I2C first = device(firstCloses);
    I2C fresh = device(freshCloses);
    I2CProvider provider = (I2CProvider) Proxy.newProxyInstance(
        I2CProvider.class.getClassLoader(), new Class<?>[] {I2CProvider.class},
        (proxy, method, args) -> {
          if (method.getName().equals("create")) {
            creates.incrementAndGet();
            return fresh;
          }
          return null;
        });
    I2CBusManager manager = new I2CBusManager(Pi4J.newContextBuilder().noAutoDetect().build(), provider, 1);
    manager.physicalDevices.put(0x23, first);
    I2CDeviceController controller = manager.configureDevice(0x23, new Bh1750Controller().getName());
    manager.close(new I2CDeviceScheduler(controller));
    assertEquals(1, firstCloses.get(), "Underlying I2C device must close once");
    assertFalse(manager.physicalDevices.containsKey(0x23));
    assertTrue(manager.getActive().isEmpty());
    I2CDeviceController remounted = manager.configureDevice(0x23, controller.getName());
    assertNotNull(remounted);
    assertEquals(1, creates.get(), "Remount must create a new handle");
    assertEquals(0, freshCloses.get());
    manager.close(controller);
    assertSame(remounted, manager.get("23"));
    assertEquals(0, freshCloses.get(), "A stale close must leave the new handle open");
    manager.close(remounted);
    assertEquals(1, freshCloses.get());
  }

  @Test
  void mountingOccupiedAddressRetainsControllerAndHandle() throws Exception {
    AtomicInteger closes = new AtomicInteger();
    I2CBusManager manager = new I2CBusManager(null, null, 1);
    manager.physicalDevices.put(0x23, device(closes));
    String name = new Bh1750Controller().getName();
    I2CDeviceController first = manager.configureDevice(0x23, name);
    assertSame(first, manager.configureDevice(0x23, name));
    assertThrows(java.io.IOException.class, () -> manager.configureDevice(0x23, "HT16K33 - 7 Segment"));
    manager.close(first);
    manager.close(first);
    assertEquals(1, closes.get());
  }

  @Test
  void displayAndPwmOverridesReleaseHandles() throws Exception {
    for (String name : new String[] {"HT16K33 - 7 Segment", new io.mapsmessaging.devices.i2c.devices.drivers.pca9685.Pca9685Controller().getName()}) {
      AtomicInteger closes = new AtomicInteger();
      I2CBusManager manager = new I2CBusManager(null, null, 1);
      manager.physicalDevices.put(0x23, device(closes));
      manager.close(manager.configureDevice(0x23, name));
      assertEquals(1, closes.get(), name);
    }
  }

  private static I2C device(AtomicInteger closes) {
    return (I2C) Proxy.newProxyInstance(I2C.class.getClassLoader(), new Class<?>[] {I2C.class},
        (proxy, method, args) -> switch (method.getName()) {
          case "getDevice" -> 0x23;
          case "getBus" -> 1;
          case "close" -> { closes.incrementAndGet(); yield null; }
          case "write" -> args.length == 3 ? args[2] : 1;
          case "read", "readRegister" -> args.length == 4 ? args[3] : args.length == 3 ? args[2] : 1;
          case "writeRegister" -> args[1] instanceof byte[] bytes ? bytes.length : 1;
          case "id", "name", "toString" -> "scripted-i2c";
          case "hashCode" -> System.identityHashCode(proxy);
          case "equals" -> proxy == args[0];
          default -> null;
        });
  }
}
