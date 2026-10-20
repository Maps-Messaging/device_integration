package io.mapsmessaging.devices.i2c.devices.sensors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.mapsmessaging.devices.i2c.devices.output.led.ht16k33.Quad7Segment;
import io.mapsmessaging.devices.i2c.devices.output.led.ht16k33.QuadAlphaNumeric;
import org.junit.jupiter.api.Test;

class DisplayEncodingTest {

  @Test
  void numericDisplayClearsTrailingDigits() throws Exception {
    Quad7Segment display = new Quad7Segment(new ScriptedI2CDevice(0x70));
    display.encode("1234");
    byte[] encoded = display.encode("1");
    for (int index = 2; index < encoded.length; index++) {
      assertEquals(0, encoded[index], "Trailing byte " + index);
    }
  }

  @Test
  void numericDecimalPointPreservesDigitSegments() throws Exception {
    Quad7Segment display = new Quad7Segment(new ScriptedI2CDevice(0x70));
    int digit = display.encode("1")[0] & 0xff;
    assertEquals(digit | 0x80, display.encode("1.")[0] & 0xff);
  }

  @Test
  void alphaDisplayClearsTrailingCharacters() throws Exception {
    QuadAlphaNumeric display = new QuadAlphaNumeric(new ScriptedI2CDevice(0x70));
    display.encode("ABCD");
    byte[] encoded = display.encode("A");
    for (int index = 2; index < encoded.length; index++) {
      assertEquals(0, encoded[index], "Trailing byte " + index);
    }
  }

  @Test
  void alphaDecimalPointPreservesCharacterSegments() throws Exception {
    QuadAlphaNumeric display = new QuadAlphaNumeric(new ScriptedI2CDevice(0x70));
    byte[] character = display.encode("A").clone();
    byte[] encoded = display.encode("A.");
    assertEquals(character[0], encoded[0]);
    assertEquals((character[1] & 0xff) | 0x40, encoded[1] & 0xff);
  }
}
