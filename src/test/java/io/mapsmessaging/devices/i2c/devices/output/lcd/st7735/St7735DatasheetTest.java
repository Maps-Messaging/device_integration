package io.mapsmessaging.devices.i2c.devices.output.lcd.st7735;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.mapsmessaging.devices.i2c.RegisterBackedI2cDevice;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class St7735DatasheetTest {

  @Test
  void controllerExecutesSetPixelAndRejectsOutOfBoundsImages() throws Exception {
    St7735Controller controller = new St7735Controller(new RegisterBackedI2cDevice(0x18));
    byte[] response = controller.updateDeviceConfiguration(
        "{\"command\":\"SET_PIXEL\",\"x\":1,\"y\":2,\"color\":\"#ff0000\"}".getBytes(StandardCharsets.UTF_8));
    assertTrue(new String(response, StandardCharsets.UTF_8).contains("success"));
    assertThrows(IOException.class, () -> controller.updateDeviceConfiguration(
        "{\"command\":\"DRAW_IMAGE\",\"x\":159,\"y\":79,\"width\":2,\"height\":1,\"data\":\"AA==\"}"
            .getBytes(StandardCharsets.UTF_8)));
  }

  @Test
  void unsupportedStorageInterfaceNoLongerClaimsSuccess() throws Exception {
    St7735Device display = new St7735Device(new RegisterBackedI2cDevice(0x18));
    assertThrows(IOException.class, () -> display.writeBlock(0, new byte[] {1}));
    assertThrows(IOException.class, () -> display.readBlock(0, 1));
  }
}
