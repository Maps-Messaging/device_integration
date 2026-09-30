package io.mapsmessaging.devices.buses;

import static org.junit.jupiter.api.Assertions.*;
import io.mapsmessaging.devices.i2c.devices.output.led.ht16k33.BlinkRate;
import io.mapsmessaging.devices.i2c.devices.output.led.ht16k33.Quad7Segment;
import io.mapsmessaging.devices.i2c.devices.output.led.ht16k33.QuadAlphaNumeric;
import org.junit.jupiter.api.Test;

class BusDeviceDisplayTest {
  @Test void decimalPointWritesLiteralSegmentBytesAndShorterTextClearsRam() throws Exception {
    BusRegisterDevice bus = new BusRegisterDevice(0x70);
    Quad7Segment display = new Quad7Segment(bus);
    display.write("1.2");
    assertArrayEquals(new byte[]{(byte) 0x86, 0, 0x5b, 0, 0, 0, 0, 0, 0, 0}, bus.image(0, 10));
    display.write("3");
    assertArrayEquals(new byte[]{0x4f, 0, 0, 0, 0, 0, 0, 0, 0, 0}, bus.image(0, 10));
  }
  @Test void customAlphaFontWritesLittleEndianWordsAndConsumesDecimalPoint() throws Exception {
    BusRegisterDevice bus = new BusRegisterDevice(0x70);
    QuadAlphaNumeric display = new QuadAlphaNumeric(bus);
    byte[] font = new byte[132];
    font[130] = 0x12;
    font[131] = 0x34;
    display.setFont(font);
    display.write("A.A");
    assertArrayEquals(new byte[]{0x34, 0x52, 0x34, 0x12, 0, 0, 0, 0}, bus.image(0, 8));
    display.write("A");
    assertArrayEquals(new byte[]{0x34, 0x12, 0, 0, 0, 0, 0, 0}, bus.image(0, 8));
  }
  @Test void brightnessBlinkAndPowerProduceHardwareCommands() throws Exception {
    BusRegisterDevice bus = new BusRegisterDevice(0x70);
    QuadAlphaNumeric display = new QuadAlphaNumeric(bus);
    bus.writes.clear();
    display.setBrightness((byte) 0x2b);
    display.setBlinkRate(BlinkRate.HALF_HZ);
    display.turnOff();
    display.turnOn();
    assertEquals(5, bus.writes.size());
    assertArrayEquals(new byte[]{(byte) 0xeb}, bus.writes.get(0));
    assertArrayEquals(new byte[]{(byte) 0x87}, bus.writes.get(1));
    assertArrayEquals(new byte[]{0x20}, bus.writes.get(2));
    assertArrayEquals(new byte[]{0x21}, bus.writes.get(3));
    assertArrayEquals(new byte[]{(byte) 0x81}, bus.writes.get(4));
  }
  @Test void rawBase64WritesDecodedBytesAtDisplayRamStart() throws Exception {
    BusRegisterDevice bus = new BusRegisterDevice(0x70);
    QuadAlphaNumeric display = new QuadAlphaNumeric(bus);
    bus.writes.clear();
    display.writeRaw("AQID/w==");
    assertArrayEquals(new byte[]{0, 1, 2, 3, (byte) 255}, bus.writes.get(0));
    assertThrows(IllegalArgumentException.class, () -> display.writeRaw("%"));
    assertEquals(1, bus.writes.size());
  }
}
