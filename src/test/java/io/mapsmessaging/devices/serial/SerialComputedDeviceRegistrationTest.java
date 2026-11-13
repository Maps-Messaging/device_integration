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
  void serialMountUsesPhysicalPortKeyAndSeparatelyRegisteredComputedController() throws Exception {
    SerialBusManager manager = new SerialBusManager();
    SerialDeviceController factory = mock(SerialDeviceController.class);
    SerialDeviceController primary = mock(SerialDeviceController.class);
    ComputedSerialDeviceController child = mock(ComputedSerialDeviceController.class);
    SerialDevice port = mock(SerialDevice.class);
    when(port.getSystemPortName()).thenReturn("COM3");
    Field field = SerialBusManager.class.getDeclaredField("knownDevices");
    field.setAccessible(true);
    ((Map<String, SerialDeviceController>) field.get(manager)).put("SEN0547", factory);
    when(factory.mount(port)).thenReturn(primary);
    when(primary.getName()).thenReturn("SEN0547");
    when(primary.hasComputedDevices()).thenReturn(true);
    doReturn(List.of(child)).when(primary).getComputedDevices();
    when(child.getName()).thenReturn("SEN0547_change");
    when(child.getComputedDeviceSuffix()).thenReturn("change");

    assertSame(primary, manager.mount("SEN0547", port));
    assertSame(primary, manager.mount("SEN0547", port));

    assertSame(primary, manager.getActive().get("COM3"));
    assertSame(child, manager.getActive().get("COM3_change"));
    assertFalse(manager.getActive().containsKey("SEN0547"));
    verify(factory, times(1)).mount(port);

    manager.unmount(child);
    assertSame(primary, manager.getActive().get("COM3"));
    assertFalse(manager.getActive().containsKey("COM3_change"));
    verify(primary, never()).close();
    verify(child).close();

    manager.unmount(primary);
    assertTrue(manager.getActive().isEmpty());
    verify(primary, never()).close();
  }

  @SuppressWarnings("unchecked")
  @Test
  void twoPortsOfSameModelHaveIndependentComputedControllers() throws Exception {
    SerialBusManager manager = new SerialBusManager();
    SerialDeviceController factory = mock(SerialDeviceController.class);
    SerialDevice firstPort = mock(SerialDevice.class);
    SerialDevice secondPort = mock(SerialDevice.class);
    when(firstPort.getSystemPortName()).thenReturn("COM3");
    when(secondPort.getSystemPortName()).thenReturn("COM4");
    SerialDeviceController first = mock(SerialDeviceController.class);
    SerialDeviceController second = mock(SerialDeviceController.class);
    ComputedSerialDeviceController firstChild = mock(ComputedSerialDeviceController.class);
    ComputedSerialDeviceController secondChild = mock(ComputedSerialDeviceController.class);
    when(factory.mount(firstPort)).thenReturn(first);
    when(factory.mount(secondPort)).thenReturn(second);
    for (SerialDeviceController controller : List.of(first, second)) {
      when(controller.getName()).thenReturn("SEN0547");
      when(controller.hasComputedDevices()).thenReturn(true);
    }
    doReturn(List.of(firstChild)).when(first).getComputedDevices();
    doReturn(List.of(secondChild)).when(second).getComputedDevices();
    for (ComputedSerialDeviceController child : List.of(firstChild, secondChild)) {
      when(child.getName()).thenReturn("SEN0547_change");
      when(child.getComputedDeviceSuffix()).thenReturn("change");
    }

    Field field = SerialBusManager.class.getDeclaredField("knownDevices");
    field.setAccessible(true);
    ((Map<String, SerialDeviceController>) field.get(manager)).put("SEN0547", factory);
    manager.mount("SEN0547", firstPort);
    manager.mount("SEN0547", secondPort);

    assertSame(first, manager.getActive().get("COM3"));
    assertSame(second, manager.getActive().get("COM4"));
    assertSame(firstChild, manager.getActive().get("COM3_change"));
    assertSame(secondChild, manager.getActive().get("COM4_change"));
    assertEquals(4, manager.getActive().size());

    manager.unmount(first);
    assertFalse(manager.getActive().containsKey("COM3"));
    assertFalse(manager.getActive().containsKey("COM3_change"));
    assertSame(second, manager.getActive().get("COM4"));
    assertSame(secondChild, manager.getActive().get("COM4_change"));
    verify(firstChild).close();
    verify(secondChild, never()).close();
  }
}
