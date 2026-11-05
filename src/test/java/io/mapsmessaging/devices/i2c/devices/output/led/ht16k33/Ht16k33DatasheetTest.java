package io.mapsmessaging.devices.i2c.devices.output.led.ht16k33;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.mapsmessaging.devices.i2c.RegisterBackedI2cDevice;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class Ht16k33DatasheetTest {

  @Test
  void panelWritesOnlyFourDigitsAndColonUsesCorrectAddress() {
    Panel panel = new Panel(4, true);
    panel.setAllDisplay(0x3f);
    panel.enableColon(true);
    byte[] ram = Base64.getDecoder().decode(panel.pack());
    assertEquals(0x3f, ram[0] & 0xff);
    assertEquals(0x3f, ram[2] & 0xff);
    assertEquals(0x02, ram[4] & 0xff);
    assertEquals(0x3f, ram[6] & 0xff);
    assertEquals(0x3f, ram[8] & 0xff);
    panel.setDisplay(-1, 0);
    panel.setDisplay(4, 0);
    panel.enableColon(false);
    assertEquals(0, Base64.getDecoder().decode(panel.pack())[4]);
  }

  @Test
  void numericDisplaySkipsColonSlotForFourDigits() throws Exception {
    Quad7Segment display = new Quad7Segment(new RegisterBackedI2cDevice(0x70));
    byte[] digits = display.encode("1234");
    assertEquals(0x06, digits[0] & 0xff);
    assertEquals(0x5b, digits[2] & 0xff);
    assertEquals(0, digits[4]);
    assertEquals(0x4f, digits[6] & 0xff);
    assertEquals(0x66, digits[8] & 0xff);

    byte[] clock = display.encode("12:34");
    assertEquals(2, clock[4]);
    assertEquals(0x4f, clock[6] & 0xff);
  }

  @Test
  void controllerAcceptsDocumentedNestedConfiguration() throws Exception {
    Quad7SegmentController controller = new Quad7SegmentController(new RegisterBackedI2cDevice(0x72));
    controller.updateDeviceConfiguration(
        "{\"deviceStatic\":{\"brightness\":7,\"blink\":\"TWO_HZ\"},\"deviceWrite\":{\"display\":\"1234\"}}"
            .getBytes(StandardCharsets.UTF_8));
    HT16K33Driver driver = (HT16K33Driver) controller.getDevice();
    assertEquals(7, driver.getBrightness());
    assertEquals(BlinkRate.TWO_HZ, driver.getRate());
    assertEquals("1234", driver.getCurrent());
  }

  @Test
  void alphanumericDisplayHandlesUnsupportedCharacters() throws Exception {
    QuadAlphaNumeric display = new QuadAlphaNumeric(new RegisterBackedI2cDevice(0x70));
    assertArrayEquals(new byte[8], display.encode("é"));
  }
}
