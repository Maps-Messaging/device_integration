/*
 * Copyright [ 2020 - 2026 ] MapsMessaging B.V.
 * Licensed under the Apache License, Version 2.0 with the Commons Clause.
 */
package io.mapsmessaging.devices;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.mapsmessaging.devices.i2c.I2CDeviceController;
import io.mapsmessaging.devices.i2cmock.I2CMockBusManager;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DebugDeviceLoadTest {

  @Test
  void mockBusMountsDemoDevicesWithoutPhysicalI2c() throws InterruptedException {
    I2CMockBusManager bus = new I2CMockBusManager(255);
    bus.scanForDevices(0);

    try {
      assertEquals(Set.of("12", "39", "62"), bus.getActive().keySet());
      for (DeviceController controller : bus.getActive().values()) {
        assertEquals(255, ((I2CDeviceController) controller).getDevice().getBus());
      }
    } finally {
      for (DeviceController controller : List.copyOf(bus.getActive().values())) {
        bus.close((I2CDeviceController) controller);
      }
    }
  }
}
