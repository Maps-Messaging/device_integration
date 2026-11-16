package io.mapsmessaging.devices.i2c.devices.sensors.bme688;

import com.pi4j.exception.Pi4JException;
import io.mapsmessaging.devices.impl.AddressableDevice;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BME688ControllerDetectionTest {

  private final BME688Controller controller = new BME688Controller();
  private final AddressableDevice device = mock(AddressableDevice.class);

  @Test
  void identifiesBme688ByChipAndVariant() {
    when(device.readRegister(0xD0)).thenReturn(0x61);
    when(device.readRegister(0xF0)).thenReturn(0x01);

    assertTrue(controller.canDetect());
    assertTrue(controller.detect(device));
  }

  @Test
  void rejectsBmp280WithoutReadingVariant() {
    when(device.readRegister(0xD0)).thenReturn(0x58);

    assertFalse(controller.detect(device));
    verify(device, never()).readRegister(0xF0);
  }

  @Test
  void rejectsOtherVariantWithSameChipId() {
    when(device.readRegister(0xD0)).thenReturn(0x61);
    when(device.readRegister(0xF0)).thenReturn(0x00);

    assertFalse(controller.detect(device));
  }

  @Test
  void rejectsHardwareReadFailure() {
    when(device.readRegister(0xD0)).thenThrow(mock(Pi4JException.class));

    assertFalse(controller.detect(device));
  }
}
