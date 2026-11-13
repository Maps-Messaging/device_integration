package io.mapsmessaging.devices.i2c;

import io.mapsmessaging.devices.DeviceController;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;

class I2CComputedDeviceLifecycleTest {
  @Test
  void closingSecondaryDoesNotClosePhysicalController() {
    I2CBusManager manager = new I2CBusManager(1);
    I2CDeviceController physical = mock(I2CDeviceController.class);
    ComputedI2CDeviceController secondary = mock(ComputedI2CDeviceController.class);
    when(physical.getMountedAddress()).thenReturn(0x3a);
    when(secondary.getMountedAddress()).thenReturn(0x3a);
    I2CDeviceScheduler wrapper = new I2CDeviceScheduler(physical);
    manager.getActive().put("3a", wrapper);
    manager.getActive().put("3a_change", secondary);

    manager.close(secondary);

    assertSame(wrapper, manager.getActive().get("3a"));
    assertNull(manager.getActive().get("3a_change"));
    verify(physical, never()).close();
    verify(secondary).close();
  }

  @Test
  void closingPhysicalRemovesAndClosesItsComputedControllers() {
    I2CBusManager manager = new I2CBusManager(1);
    I2CDeviceController physical = mock(I2CDeviceController.class);
    ComputedI2CDeviceController secondary = mock(ComputedI2CDeviceController.class);
    when(physical.getMountedAddress()).thenReturn(0x3a);
    I2CDeviceScheduler wrapper = new I2CDeviceScheduler(physical);
    manager.getActive().put("3a", wrapper);
    manager.getActive().put("3a_change", secondary);

    manager.close(wrapper);

    assertNull(manager.getActive().get("3a"));
    assertNull(manager.getActive().get("3a_change"));
    verify(physical).close();
    verify(secondary).close();
  }
}
