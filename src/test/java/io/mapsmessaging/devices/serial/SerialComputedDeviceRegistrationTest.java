package io.mapsmessaging.devices.serial;

import io.mapsmessaging.devices.DeviceController;
import io.mapsmessaging.devices.serial.devices.sensors.SerialDevice;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;

class SerialComputedDeviceRegistrationTest {

  @SuppressWarnings("unchecked")
  @Test
  void serialMountExposesComputedControllerWithoutAnotherHardwareMount() throws Exception {
    SerialBusManager manager = new SerialBusManager();
    SerialDeviceController factory = mock(SerialDeviceController.class);
    SerialDeviceController primary = mock(SerialDeviceController.class);
    ComputedSerialDeviceController child = mock(ComputedSerialDeviceController.class);
    SerialDevice port = mock(SerialDevice.class);
    Field field = SerialBusManager.class.getDeclaredField("knownDevices");
    field.setAccessible(true);
    ((Map<String, SerialDeviceController>) field.get(manager)).put("SEN0547", factory);
    when(factory.mount(port)).thenReturn(primary);
    when(primary.getName()).thenReturn("SEN0547");
    when(primary.hasComputedDevices()).thenReturn(true);
    doReturn(List.of(child)).when(primary).getComputedDevices();
    when(child.getName()).thenReturn("SEN0547_change");
    when(child.getComputedDeviceSuffix()).thenReturn("change");

    manager.mount("SEN0547", port);

    assertSame(primary, manager.getActive().get("SEN0547"));
    assertSame(child, manager.getActive().get("SEN0547_change"));
    verify(factory, times(1)).mount(port);

    manager.unmount(child);
    assertSame(primary, manager.getActive().get("SEN0547"));
    assertFalse(manager.getActive().containsKey("SEN0547_change"));
    verify(primary, never()).close();

    manager.unmount(primary);
    assertTrue(manager.getActive().isEmpty());
  }
}
