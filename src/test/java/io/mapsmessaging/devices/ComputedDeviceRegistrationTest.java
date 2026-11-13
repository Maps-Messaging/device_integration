package io.mapsmessaging.devices;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.mapsmessaging.devices.serial.ComputedSerialDeviceController;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ComputedDeviceRegistrationTest {

  @Test
  void ordinaryControllersExposeNoComputedDevices() {
    DeviceController controller = mock(DeviceController.class, CALLS_REAL_METHODS);
    assertFalse(controller.hasComputedDevices());
    assertTrue(controller.getComputedDevices().isEmpty());
    assertEquals("", controller.getComputedDeviceSuffix());
  }

  @Test
  void registerDistinctLogicalControllersWithoutReplacingPhysicalKey() throws Exception {
    DeviceController primary = mock(DeviceController.class);
    ComputedSerialDeviceController child = mock(ComputedSerialDeviceController.class);
    when(primary.hasComputedDevices()).thenReturn(true);
    when(primary.getName()).thenReturn("SEN0547");
    doReturn(List.of(child)).when(primary).getComputedDevices();
    when(child.getName()).thenReturn("SEN0547_change");
    when(child.getComputedDeviceSuffix()).thenReturn("change");

    Map<String, DeviceController> entries = ComputedDeviceRegistration.collect(
        primary, "SEN0547", DeviceController.class, Map.of("SEN0547", primary));
    assertSame(child, entries.get("SEN0547_change"));
    assertEquals(1, entries.size());
  }

  @Test
  void invalidNamesAndDuplicateSuffixesFailBeforeRegistration() {
    DeviceController primary = mock(DeviceController.class);
    DeviceController first = mock(DeviceController.class);
    DeviceController second = mock(DeviceController.class);
    when(primary.hasComputedDevices()).thenReturn(true);
    when(primary.getName()).thenReturn("RADAR");
    when(first.getComputedDeviceSuffix()).thenReturn("change");
    when(first.getName()).thenReturn("RADAR_change");
    when(second.getComputedDeviceSuffix()).thenReturn("change");
    when(second.getName()).thenReturn("RADAR_change");
    doReturn(List.of(first, second)).when(primary).getComputedDevices();

    assertThrows(IOException.class, () ->
        ComputedDeviceRegistration.collect(primary, "radar", DeviceController.class, Map.of()));

    doReturn(List.of(first)).when(primary).getComputedDevices();
    assertThrows(IOException.class, () ->
        ComputedDeviceRegistration.collect(primary, "radar", DeviceController.class,
            Map.of("radar_change", first)));

    when(first.getComputedDeviceSuffix()).thenReturn("../bad");
    assertThrows(IOException.class, () ->
        ComputedDeviceRegistration.collect(primary, "radar", DeviceController.class, Map.of()));
  }
}
