package io.mapsmessaging.devices.i2c;

import io.mapsmessaging.devices.DeviceType;
import io.mapsmessaging.devices.impl.AddressableDevice;
import io.mapsmessaging.schemas.config.SchemaConfig;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;

class ComputedI2CControllerLifecycleTest {
  @Test
  void closeIsLocalAndSubsequentReadsFailWithoutTouchingHardware() throws Exception {
    AddressableDevice address = mock(AddressableDevice.class);
    when(address.getDevice()).thenReturn(0x3a);
    I2CDevice physical = mock(I2CDevice.class);
    Secondary controller = new Secondary(address, physical);
    assertEquals(0x3a, controller.getMountedAddress());
    assertArrayEquals(new byte[]{3}, controller.getDeviceState());
    controller.close();
    assertTrue(controller.isComputedClosed());
    assertThrows(IOException.class, controller::getDeviceState);
    assertThrows(IOException.class, controller::getDeviceConfiguration);
    assertThrows(IOException.class, () -> controller.updateDeviceConfiguration(new byte[0]));
    verify(physical, never()).close();
  }

  private static final class Secondary extends ComputedI2CDeviceController {
    private final I2CDevice physical;

    Secondary(AddressableDevice address, I2CDevice physical) {
      super(address);
      this.physical = physical;
    }

    @Override protected byte[] readComputedState() { return new byte[]{3}; }
    @Override public String getComputedDeviceSuffix() { return "change"; }
    @Override public String getName() { return "TEST_change"; }
    @Override public String getDescription() { return "Computed test device"; }
    @Override public SchemaConfig getSchema() { return null; }
    @Override public DeviceType getType() { return DeviceType.SENSOR; }
    @Override public I2CDevice getDevice() { return physical; }
    @Override public I2CDeviceController mount(AddressableDevice device) { return this; }
    @Override public int[] getAddressRange() { return new int[]{0x3a}; }
    @Override public boolean detect(AddressableDevice device) { return false; }
  }
}
